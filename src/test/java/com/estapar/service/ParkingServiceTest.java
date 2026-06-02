package com.estapar.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.estapar.dto.EntryEventDTO;
import com.estapar.dto.ExitEventDTO;
import com.estapar.dto.ParkedEventDTO;
import com.estapar.entity.GarageSector;
import com.estapar.entity.ParkingSpot;
import com.estapar.entity.Revenue;
import com.estapar.entity.Vehicle;
import com.estapar.enums.EventType;
import com.estapar.enums.SpotStatus;
import com.estapar.enums.VehicleStatus;
import com.estapar.exception.ParkingFullException;
import com.estapar.exception.VehicleNotFoundException;
import com.estapar.repository.GarageSectorRepository;
import com.estapar.repository.ParkingSpotRepository;
import com.estapar.repository.RevenueRepository;
import com.estapar.repository.VehicleRepository;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

	@Mock
	private GarageSectorRepository sectorRepository;

	@Mock
	private ParkingSpotRepository spotRepository;

	@Mock
	private VehicleRepository vehicleRepository;

	@Mock
	private RevenueRepository revenueRepository;

	@Mock
	private PricingService pricingService;

	@InjectMocks
	private ParkingService parkingService;

	private GarageSector sector;
	private ParkingSpot spot;
	private Instant entryTime;

	@BeforeEach
	void setUp() {
		sector = new GarageSector();
		sector.setSector("A");
		sector.setBasePrice(new BigDecimal("10.00"));
		sector.setMaxCapacity(100);

		spot = new ParkingSpot();
		spot.setId(1L);
		spot.setSector("A");
		spot.setLat(-23.561684);
		spot.setLng(-46.655981);
		spot.setStatus(SpotStatus.AVAILABLE);

		entryTime = Instant.parse("2025-01-01T12:00:00.000Z");
	}

	@Test
	void shouldRegisterEntryAndOccupySpot() {
		EntryEventDTO event = new EntryEventDTO("ABC1234", entryTime, EventType.ENTRY);

		when(vehicleRepository.existsByLicensePlateAndStatusIn(eq("ABC1234"), any())).thenReturn(false);
		when(sectorRepository.findAllByOrderBySectorAsc()).thenReturn(List.of(sector));
		when(vehicleRepository.countBySectorAndStatusIn(eq("A"), any())).thenReturn(0L);
		when(spotRepository.findFirstBySectorAndStatusOrderByIdAsc("A", SpotStatus.AVAILABLE))
				.thenReturn(Optional.of(spot));
		when(pricingService.calculateOccupancy(0, 100)).thenReturn(0.0);
		when(pricingService.calculateHourlyRate(sector.getBasePrice(), 0.0))
				.thenReturn(new BigDecimal("9.00"));

		parkingService.handleEntry(event);

		assertEquals(SpotStatus.OCCUPIED, spot.getStatus());
		verify(spotRepository).save(spot);

		ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
		verify(vehicleRepository).save(vehicleCaptor.capture());
		Vehicle saved = vehicleCaptor.getValue();
		assertEquals("ABC1234", saved.getLicensePlate());
		assertEquals(VehicleStatus.ENTERED, saved.getStatus());
		assertEquals(new BigDecimal("9.00"), saved.getHourlyRate());
		assertEquals(1L, saved.getSpotId());
	}

	@Test
	void shouldRejectEntryWhenSectorFull() {
		EntryEventDTO event = new EntryEventDTO("ABC1234", entryTime, EventType.ENTRY);

		when(vehicleRepository.existsByLicensePlateAndStatusIn(eq("ABC1234"), any())).thenReturn(false);
		when(sectorRepository.findAllByOrderBySectorAsc()).thenReturn(List.of(sector));
		when(vehicleRepository.countBySectorAndStatusIn(eq("A"), any())).thenReturn(100L);

		assertThrows(ParkingFullException.class, () -> parkingService.handleEntry(event));
		verify(vehicleRepository, never()).save(any());
	}

	@Test
	void shouldProcessExitChargeRevenueAndReleaseSpot() {
		Instant exitTime = entryTime.plusSeconds(7200);
		ExitEventDTO event = new ExitEventDTO("ABC1234", exitTime, EventType.EXIT);

		Vehicle vehicle = new Vehicle();
		vehicle.setLicensePlate("ABC1234");
		vehicle.setEntryTime(entryTime);
		vehicle.setSector("A");
		vehicle.setSpotId(1L);
		vehicle.setStatus(VehicleStatus.PARKED);
		vehicle.setHourlyRate(new BigDecimal("10.00"));

		when(vehicleRepository.findByLicensePlateAndStatusIn(eq("ABC1234"), any()))
				.thenReturn(Optional.of(vehicle));
		when(pricingService.calculateParkingFee(new BigDecimal("10.00"), 120))
				.thenReturn(new BigDecimal("10.00"));
		when(spotRepository.findById(1L)).thenReturn(Optional.of(spot));

		parkingService.handleExit(event);

		assertEquals(SpotStatus.AVAILABLE, spot.getStatus());
		assertEquals(VehicleStatus.EXITED, vehicle.getStatus());
		verify(revenueRepository).save(any(Revenue.class));
	}

	@Test
	void shouldNotSaveRevenueWhenExitWithin30Minutes() {
		Instant exitTime = entryTime.plusSeconds(1800);
		ExitEventDTO event = new ExitEventDTO("ABC1234", exitTime, EventType.EXIT);

		Vehicle vehicle = new Vehicle();
		vehicle.setLicensePlate("ABC1234");
		vehicle.setEntryTime(entryTime);
		vehicle.setSector("A");
		vehicle.setSpotId(1L);
		vehicle.setStatus(VehicleStatus.ENTERED);
		vehicle.setHourlyRate(new BigDecimal("10.00"));

		when(vehicleRepository.findByLicensePlateAndStatusIn(eq("ABC1234"), any()))
				.thenReturn(Optional.of(vehicle));
		when(pricingService.calculateParkingFee(new BigDecimal("10.00"), 30))
				.thenReturn(BigDecimal.ZERO);
		when(spotRepository.findById(1L)).thenReturn(Optional.of(spot));

		parkingService.handleExit(event);

		verify(revenueRepository, never()).save(any());
		assertEquals(VehicleStatus.EXITED, vehicle.getStatus());
	}

	@Test
	void shouldUpdateSpotOnParkedEvent() {
		ParkedEventDTO event = new ParkedEventDTO("ABC1234", -23.561684, -46.655981, EventType.PARKED);

		Vehicle vehicle = new Vehicle();
		vehicle.setLicensePlate("ABC1234");
		vehicle.setSector("A");
		vehicle.setSpotId(2L);
		vehicle.setStatus(VehicleStatus.ENTERED);

		ParkingSpot targetSpot = new ParkingSpot();
		targetSpot.setId(1L);
		targetSpot.setSector("A");
		targetSpot.setLat(-23.561684);
		targetSpot.setLng(-46.655981);
		targetSpot.setStatus(SpotStatus.AVAILABLE);

		ParkingSpot oldSpot = new ParkingSpot();
		oldSpot.setId(2L);
		oldSpot.setStatus(SpotStatus.OCCUPIED);

		when(vehicleRepository.findByLicensePlateAndStatusIn(eq("ABC1234"), any()))
				.thenReturn(Optional.of(vehicle));
		when(spotRepository.findByLatAndLng(-23.561684, -46.655981)).thenReturn(Optional.of(targetSpot));
		when(spotRepository.findById(2L)).thenReturn(Optional.of(oldSpot));

		parkingService.handleParked(event);

		assertEquals(1L, vehicle.getSpotId());
		assertEquals(VehicleStatus.PARKED, vehicle.getStatus());
		assertEquals(SpotStatus.AVAILABLE, oldSpot.getStatus());
		assertEquals(SpotStatus.OCCUPIED, targetSpot.getStatus());
	}

	@Test
	void shouldThrowWhenVehicleNotFoundOnExit() {
		ExitEventDTO event = new ExitEventDTO("XYZ9999", entryTime, EventType.EXIT);
		when(vehicleRepository.findByLicensePlateAndStatusIn(eq("XYZ9999"), any()))
				.thenReturn(Optional.empty());

		assertThrows(VehicleNotFoundException.class, () -> parkingService.handleExit(event));
	}

}
