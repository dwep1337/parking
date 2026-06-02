package com.estapar.service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.estapar.dto.EntryEventDTO;
import com.estapar.dto.ExitEventDTO;
import com.estapar.dto.ParkedEventDTO;
import com.estapar.entity.GarageSector;
import com.estapar.entity.ParkingSpot;
import com.estapar.entity.Revenue;
import com.estapar.entity.Vehicle;
import com.estapar.enums.SpotStatus;
import com.estapar.enums.VehicleStatus;
import com.estapar.exception.ParkingFullException;
import com.estapar.exception.VehicleNotFoundException;
import com.estapar.repository.GarageSectorRepository;
import com.estapar.repository.ParkingSpotRepository;
import com.estapar.repository.RevenueRepository;
import com.estapar.repository.VehicleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParkingService {

	private static final List<VehicleStatus> ACTIVE_STATUSES = List.of(
			VehicleStatus.ENTERED,
			VehicleStatus.PARKED);

	private final GarageSectorRepository sectorRepository;
	private final ParkingSpotRepository spotRepository;
	private final VehicleRepository vehicleRepository;
	private final RevenueRepository revenueRepository;
	private final PricingService pricingService;

	@Transactional
	public void handleEntry(EntryEventDTO event) {
		validateLicensePlate(event.licensePlate());

		if (vehicleRepository.existsByLicensePlateAndStatusIn(event.licensePlate(), ACTIVE_STATUSES)) {
			throw new ParkingFullException("Veículo já possui sessão ativa: " + event.licensePlate());
		}

		GarageSector sector = findAvailableSector()
				.orElseThrow(() -> new ParkingFullException("Estacionamento lotado"));

		long occupied = vehicleRepository.countBySectorAndStatusIn(sector.getSector(), ACTIVE_STATUSES);
		if (occupied >= sector.getMaxCapacity()) {
			throw new ParkingFullException("Setor " + sector.getSector() + " lotado");
		}

		ParkingSpot spot = spotRepository
				.findFirstBySectorAndStatusOrderByIdAsc(sector.getSector(), SpotStatus.AVAILABLE)
				.orElseThrow(() -> new ParkingFullException(
						"Nenhuma vaga disponível no setor " + sector.getSector()));

		double occupancy = pricingService.calculateOccupancy(occupied, sector.getMaxCapacity());
		BigDecimal hourlyRate = pricingService.calculateHourlyRate(sector.getBasePrice(), occupancy);

		spot.setStatus(SpotStatus.OCCUPIED);
		spotRepository.save(spot);

		Vehicle vehicle = new Vehicle();
		vehicle.setLicensePlate(event.licensePlate());
		vehicle.setEntryTime(event.entryTime());
		vehicle.setSector(sector.getSector());
		vehicle.setSpotId(spot.getId());
		vehicle.setStatus(VehicleStatus.ENTERED);
		vehicle.setHourlyRate(hourlyRate);
		vehicleRepository.save(vehicle);

		log.info("ENTRY placa={} setor={} vaga={} tarifa={} ocupacao={}",
				event.licensePlate(), sector.getSector(), spot.getId(), hourlyRate, occupancy);
	}

	@Transactional
	public void handleParked(ParkedEventDTO event) {
		validateLicensePlate(event.licensePlate());

		if (event.lat() == null || event.lng() == null) {
			throw new IllegalArgumentException("Coordenadas são obrigatórias para o evento PARKED");
		}

		Vehicle vehicle = findActiveVehicle(event.licensePlate());

		ParkingSpot targetSpot = spotRepository.findByLatAndLng(event.lat(), event.lng())
				.orElseThrow(() -> new VehicleNotFoundException(
						"Vaga não encontrada para as coordenadas informadas"));

		Long currentSpotId = vehicle.getSpotId();
		if (currentSpotId != null && !currentSpotId.equals(targetSpot.getId())) {
			spotRepository.findById(currentSpotId).ifPresent(this::releaseSpot);
			targetSpot.setStatus(SpotStatus.OCCUPIED);
			spotRepository.save(targetSpot);
			vehicle.setSpotId(targetSpot.getId());
			vehicle.setSector(targetSpot.getSector());
		}
		else if (currentSpotId == null) {
			targetSpot.setStatus(SpotStatus.OCCUPIED);
			spotRepository.save(targetSpot);
			vehicle.setSpotId(targetSpot.getId());
			vehicle.setSector(targetSpot.getSector());
		}

		vehicle.setStatus(VehicleStatus.PARKED);
		vehicleRepository.save(vehicle);

		log.info("PARKED placa={} vaga={}", event.licensePlate(), targetSpot.getId());
	}

	@Transactional
	public void handleExit(ExitEventDTO event) {
		validateLicensePlate(event.licensePlate());

		Vehicle vehicle = findActiveVehicle(event.licensePlate());

		long durationMinutes = ChronoUnit.MINUTES.between(vehicle.getEntryTime(), event.exitTime());
		BigDecimal amount = pricingService.calculateParkingFee(vehicle.getHourlyRate(), durationMinutes);

		if (vehicle.getSpotId() != null) {
			spotRepository.findById(vehicle.getSpotId()).ifPresent(this::releaseSpot);
		}

		if (amount.compareTo(BigDecimal.ZERO) > 0) {
			Revenue revenue = new Revenue();
			revenue.setSector(vehicle.getSector());
			revenue.setAmount(amount);
			revenue.setCreatedAt(event.exitTime());
			revenueRepository.save(revenue);
		}

		vehicle.setExitTime(event.exitTime());
		vehicle.setStatus(VehicleStatus.EXITED);
		vehicleRepository.save(vehicle);

		log.info("EXIT placa={} valor={} minutos={}", event.licensePlate(), amount, durationMinutes);
	}

	private Optional<GarageSector> findAvailableSector() {
		return sectorRepository.findAllByOrderBySectorAsc().stream()
				.filter(sector -> {
					long occupied = vehicleRepository.countBySectorAndStatusIn(
							sector.getSector(), ACTIVE_STATUSES);
					if (occupied >= sector.getMaxCapacity()) {
						return false;
					}
					return spotRepository
							.findFirstBySectorAndStatusOrderByIdAsc(sector.getSector(), SpotStatus.AVAILABLE)
							.isPresent();
				})
				.findFirst();
	}

	private Vehicle findActiveVehicle(String licensePlate) {
		return vehicleRepository.findByLicensePlateAndStatusIn(licensePlate, ACTIVE_STATUSES)
				.orElseThrow(() -> new VehicleNotFoundException(
						"Veículo não encontrado ou sem sessão ativa: " + licensePlate));
	}

	private void releaseSpot(ParkingSpot spot) {
		spot.setStatus(SpotStatus.AVAILABLE);
		spotRepository.save(spot);
	}

	private void validateLicensePlate(String licensePlate) {
		if (licensePlate == null || licensePlate.isBlank()) {
			throw new IllegalArgumentException("Placa do veículo é obrigatória");
		}
	}

}
