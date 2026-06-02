package com.estapar.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GarageSectorDTO(
		String sector,
		@JsonAlias({ "base_price", "basePrice" }) BigDecimal basePrice,
		@JsonProperty("max_capacity") int maxCapacity) {
}
