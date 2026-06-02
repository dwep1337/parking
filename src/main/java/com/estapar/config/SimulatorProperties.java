package com.estapar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "simulator")
public class SimulatorProperties {

	private String baseUrl = "http://localhost:3000";

	private Retry retry = new Retry();

	@Getter
	@Setter
	public static class Retry {

		private long delayMs = 5000;

		private int maxAttempts = 0;

	}

}
