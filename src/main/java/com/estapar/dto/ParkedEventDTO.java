package com.estapar.dto;

import com.estapar.enums.EventType;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ParkedEventDTO(
		@JsonProperty("license_plate") String licensePlate,
		Double lat,
		Double lng,
		@JsonProperty("event_type") EventType eventType) {
}
