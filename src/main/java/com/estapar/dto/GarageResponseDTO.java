package com.estapar.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GarageResponseDTO(
		List<GarageSectorDTO> garage,
		List<ParkingSpotDTO> spots) {
}
