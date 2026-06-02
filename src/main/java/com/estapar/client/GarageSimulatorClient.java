package com.estapar.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.estapar.dto.GarageResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class GarageSimulatorClient {

	private final RestClient simulatorRestClient;
	private final JsonMapper jsonMapper;

	public GarageResponseDTO fetchGarage() {
		String rawBody = simulatorRestClient.get()
				.uri("/garage")
				.retrieve()
				.body(String.class);

		log.info("GET /garage response: {}", rawBody);

		try {
			return jsonMapper.readValue(rawBody, GarageResponseDTO.class);
		}
		catch (Exception ex) {
			throw new IllegalStateException("Resposta inválida do simulador em GET /garage", ex);
		}
	}

}
