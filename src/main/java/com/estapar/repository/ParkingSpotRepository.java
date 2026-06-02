package com.estapar.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.estapar.entity.ParkingSpot;
import com.estapar.enums.SpotStatus;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, Long> {

	Optional<ParkingSpot> findFirstBySectorAndStatusOrderByIdAsc(String sector, SpotStatus status);

	Optional<ParkingSpot> findByLatAndLng(Double lat, Double lng);

	long countBySectorAndStatus(String sector, SpotStatus status);

}
