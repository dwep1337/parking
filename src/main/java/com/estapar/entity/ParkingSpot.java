package com.estapar.entity;

import com.estapar.enums.SpotStatus;

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
@Table(name = "parking_spot")
@Getter
@Setter
@NoArgsConstructor
public class ParkingSpot {

	@Id
	private Long id;

	@Column(nullable = false)
	private String sector;

	@Column(nullable = false)
	private Double lat;

	@Column(nullable = false)
	private Double lng;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SpotStatus status;

}
