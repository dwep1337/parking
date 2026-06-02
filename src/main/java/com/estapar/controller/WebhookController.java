package com.estapar.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.estapar.dto.EntryEventDTO;
import com.estapar.dto.ExitEventDTO;
import com.estapar.dto.ParkedEventDTO;
import com.estapar.enums.EventType;
import com.estapar.exception.ParkingFullException;
import com.estapar.exception.VehicleNotFoundException;
import com.estapar.service.ParkingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebhookController {

	private final ParkingService parkingService;
	private final JsonMapper jsonMapper;

	@PostMapping("/webhook")
	public ResponseEntity<Void> handleWebhook(@RequestBody JsonNode body) {
		EventType eventType = EventType.valueOf(body.get("event_type").asString());

		switch (eventType) {
			case ENTRY -> {
				EntryEventDTO event = jsonMapper.convertValue(body, EntryEventDTO.class);
				runSafely(() -> parkingService.handleEntry(event), "ENTRY");
			}
			case PARKED -> {
				ParkedEventDTO event = jsonMapper.convertValue(body, ParkedEventDTO.class);
				runSafely(() -> parkingService.handleParked(event), "PARKED");
			}
			case EXIT -> {
				ExitEventDTO event = jsonMapper.convertValue(body, ExitEventDTO.class);
				runSafely(() -> parkingService.handleExit(event), "EXIT");
			}
		}

		return ResponseEntity.ok().build();
	}

	private void runSafely(Runnable action, String eventLabel) {
		try {
			action.run();
		}
		catch (ParkingFullException | VehicleNotFoundException | IllegalArgumentException ex) {
			log.warn("Webhook {} ignorado: {}", eventLabel, ex.getMessage());
		}
	}

}
