package com.estapar.dto;

import java.time.Instant;

import com.estapar.enums.EventType;
import com.fasterxml.jackson.annotation.JsonProperty;

public record EntryEventDTO(
		@JsonProperty("license_plate") String licensePlate,
		@JsonProperty("entry_time") Instant entryTime,
		@JsonProperty("event_type") EventType eventType) {
}
