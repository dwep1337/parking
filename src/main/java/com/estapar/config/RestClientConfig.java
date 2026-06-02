package com.estapar.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(SimulatorProperties.class)
public class RestClientConfig {

	@Bean
	RestClient simulatorRestClient(SimulatorProperties properties) {
		return RestClient.builder()
				.baseUrl(properties.getBaseUrl())
				.build();
	}

}
