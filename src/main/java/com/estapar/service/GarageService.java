package com.estapar.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.estapar.client.GarageSimulatorClient;
import com.estapar.config.SimulatorProperties;
import com.estapar.dto.GarageResponseDTO;
import com.estapar.dto.GarageSectorDTO;
import com.estapar.dto.ParkingSpotDTO;
import com.estapar.entity.GarageSector;
import com.estapar.entity.ParkingSpot;
import com.estapar.enums.SpotStatus;
import com.estapar.enums.VehicleStatus;
import com.estapar.repository.GarageSectorRepository;
import com.estapar.repository.ParkingSpotRepository;
import com.estapar.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GarageService {

	private static final List<VehicleStatus> ACTIVE_STATUSES = List.of(
			VehicleStatus.ENTERED,
			VehicleStatus.PARKED);

	private final GarageSimulatorClient simulatorClient;
	private final GarageSectorRepository sectorRepository;
	private final ParkingSpotRepository spotRepository;
	private final VehicleRepository vehicleRepository;
	private final SimulatorProperties properties;

	@Bean
	ApplicationRunner garageBootstrapRunner(GarageService garageService) {
		return args -> {
			log.info("Iniciando sincronização da garagem com o simulador...");
			garageService.syncWithRetry();
		};
	}

	public void syncWithRetry() {
		int attempt = 0;
		SimulatorProperties.Retry retry = properties.getRetry();

		while (true) {
			attempt++;
			try {
				syncFromSimulator();
				log.info("Garagem sincronizada na tentativa {}", attempt);
				return;
			}
			catch (Exception ex) {
				boolean hasLimit = retry.getMaxAttempts() > 0;
				boolean limitReached = hasLimit && attempt >= retry.getMaxAttempts();

				if (limitReached) {
					throw new IllegalStateException(
							"Não foi possível sincronizar a garagem após " + attempt + " tentativas", ex);
				}

				log.warn("Simulador indisponível (tentativa {}): {}. Nova tentativa em {} ms",
						attempt, ex.getMessage(), retry.getDelayMs());
				sleep(retry.getDelayMs());
			}
		}
	}

	@Transactional
	public void syncFromSimulator() {
		GarageResponseDTO response = simulatorClient.fetchGarage();

		Map<String, GarageSector> sectorsByCode = response.garage().stream()
				.map(this::saveSector)
				.collect(Collectors.toMap(GarageSector::getSector, Function.identity()));

		for (ParkingSpotDTO spotDto : response.spots()) {
			if (!sectorsByCode.containsKey(spotDto.sector())) {
				throw new IllegalStateException("Setor não encontrado para a vaga " + spotDto.id());
			}
			saveSpot(spotDto);
		}

		log.info("Garagem sincronizada: {} setores, {} vagas",
				response.garage().size(), response.spots().size());
	}

	private GarageSector saveSector(GarageSectorDTO dto) {
		GarageSector sector = sectorRepository.findById(dto.sector())
				.orElseGet(GarageSector::new);

		sector.setSector(dto.sector());
		sector.setBasePrice(dto.basePrice());
		sector.setMaxCapacity(dto.maxCapacity());

		return sectorRepository.save(sector);
	}

	private void saveSpot(ParkingSpotDTO dto) {
		ParkingSpot spot = spotRepository.findById(dto.id())
				.orElseGet(ParkingSpot::new);

		spot.setId(dto.id());
		spot.setSector(dto.sector());
		spot.setLat(dto.lat());
		spot.setLng(dto.lng());

		if (spot.getStatus() == null || !hasActiveVehicleOnSpot(dto.id())) {
			spot.setStatus(SpotStatus.AVAILABLE);
		}

		spotRepository.save(spot);
	}

	private boolean hasActiveVehicleOnSpot(Long spotId) {
		return vehicleRepository.findAll().stream()
				.anyMatch(v -> spotId.equals(v.getSpotId())
						&& ACTIVE_STATUSES.contains(v.getStatus()));
	}

	private void sleep(long delayMs) {
		try {
			Thread.sleep(delayMs);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Sincronização da garagem interrompida", ex);
		}
	}

}
