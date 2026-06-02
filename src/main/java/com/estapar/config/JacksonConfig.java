package com.estapar.config;

import java.time.Instant;
import java.util.List;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

@Configuration
public class JacksonConfig {

	@Bean
	JsonMapperBuilderCustomizer utcInstantDeserializer() {
		return builder -> builder.addModule(
				new SimpleModule("UtcInstantModule").addDeserializer(Instant.class, new UtcInstantDeserializer()));
	}

	@Bean
	JsonMapper jsonMapper(List<JsonMapperBuilderCustomizer> customizers) {
		JsonMapper.Builder builder = JsonMapper.builder();
		customizers.forEach(customizer -> customizer.customize(builder));
		return builder.build();
	}

}
