package com.estapar.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.estapar.config.UtcInstantDeserializer;
import com.estapar.exception.ParkingFullException;
import com.estapar.exception.VehicleNotFoundException;
import com.estapar.service.ParkingService;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

@ExtendWith(MockitoExtension.class)
class WebhookControllerTest {

	@Mock
	private ParkingService parkingService;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		JsonMapper jsonMapper = JsonMapper.builder()
				.addModule(new SimpleModule("UtcInstantModule")
						.addDeserializer(java.time.Instant.class, new UtcInstantDeserializer()))
				.build();

		WebhookController controller = new WebhookController(parkingService, jsonMapper);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	void shouldReturn200WhenEntryRejectedDueToFullParking() throws Exception {
		doThrow(new ParkingFullException("Estacionamento lotado"))
				.when(parkingService).handleEntry(any());

		mockMvc.perform(post("/webhook")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "license_plate": "ABC1234",
								  "entry_time": "2025-01-01T12:00:00.000Z",
								  "event_type": "ENTRY"
								}
								"""))
				.andExpect(status().isOk());
	}

	@Test
	void shouldReturn200WhenExitVehicleNotFound() throws Exception {
		doThrow(new VehicleNotFoundException("Veículo não encontrado"))
				.when(parkingService).handleExit(any());

		mockMvc.perform(post("/webhook")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "license_plate": "XYZ9999",
								  "exit_time": "2025-01-01T12:00:00.000Z",
								  "event_type": "EXIT"
								}
								"""))
				.andExpect(status().isOk());
	}

	@Test
	void shouldReturn200WhenParkedFailsValidation() throws Exception {
		doThrow(new IllegalArgumentException("Coordenadas são obrigatórias"))
				.when(parkingService).handleParked(any());

		mockMvc.perform(post("/webhook")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "license_plate": "ABC1234",
								  "event_type": "PARKED"
								}
								"""))
				.andExpect(status().isOk());
	}

}
