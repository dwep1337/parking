package com.estapar.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ParkingSpotDTO(
		Long id,
		String sector,
		double lat,
		double lng) {
}
