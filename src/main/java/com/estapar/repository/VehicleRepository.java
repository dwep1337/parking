package com.estapar.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.estapar.entity.Vehicle;
import com.estapar.enums.VehicleStatus;

public interface VehicleRepository extends JpaRepository<Vehicle, String> {

	Optional<Vehicle> findByLicensePlateAndStatusIn(String licensePlate, Collection<VehicleStatus> statuses);

	boolean existsByLicensePlateAndStatusIn(String licensePlate, Collection<VehicleStatus> statuses);

	long countBySectorAndStatusIn(String sector, Collection<VehicleStatus> statuses);

}
