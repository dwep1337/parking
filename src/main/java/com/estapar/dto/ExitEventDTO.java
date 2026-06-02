package com.estapar.dto;

import java.time.Instant;

import com.estapar.enums.EventType;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ExitEventDTO(
		@JsonProperty("license_plate") String licensePlate,
		@JsonProperty("exit_time") Instant exitTime,
		@JsonProperty("event_type") EventType eventType) {
}
