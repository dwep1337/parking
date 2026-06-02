package com.estapar.entity;

import java.math.BigDecimal;
import java.time.Instant;

import com.estapar.enums.VehicleStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicle")
@Getter
@Setter
@NoArgsConstructor
public class Vehicle {

	@Id
	@Column(name = "license_plate")
	private String licensePlate;

	@Column(name = "entry_time", nullable = false)
	private Instant entryTime;

	@Column(name = "exit_time")
	private Instant exitTime;

	@Column(nullable = false)
	private String sector;

	@Column(name = "spot_id")
	private Long spotId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private VehicleStatus status;

	@Column(name = "hourly_rate", nullable = false)
	private BigDecimal hourlyRate;

}
