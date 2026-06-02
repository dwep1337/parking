package com.estapar.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.estapar.dto.RevenueResponseDTO;
import com.estapar.exception.GlobalExceptionHandler;
import com.estapar.service.RevenueService;

@ExtendWith(MockitoExtension.class)
class RevenueControllerTest {

	@Mock
	private RevenueService revenueService;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		RevenueController controller = new RevenueController(revenueService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new GlobalExceptionHandler())
				.setMessageConverters(new JacksonJsonHttpMessageConverter())
				.build();
	}

	@Test
	void shouldGetRevenueByQueryParams() throws Exception {
		var response = new RevenueResponseDTO(
				new BigDecimal("42.50"),
				"BRL",
				Instant.parse("2025-01-01T12:00:00.000Z"));

		when(revenueService.getRevenue(eq(LocalDate.parse("2025-01-01")), eq("A")))
				.thenReturn(response);

		mockMvc.perform(get("/revenue")
						.param("date", "2025-01-01")
						.param("sector", "A"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.amount").value(42.50))
				.andExpect(jsonPath("$.currency").value("BRL"));
	}

	@Test
	void shouldGetRevenueByJsonBody() throws Exception {
		var response = new RevenueResponseDTO(
				BigDecimal.ZERO.setScale(2),
				"BRL",
				Instant.parse("2025-01-01T12:00:00.000Z"));

		when(revenueService.getRevenue(eq(LocalDate.parse("2025-01-01")), eq("A")))
				.thenReturn(response);

		mockMvc.perform(get("/revenue")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "date": "2025-01-01",
								  "sector": "A"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.amount").value(0.0))
				.andExpect(jsonPath("$.currency").value("BRL"));
	}

	@Test
	void shouldReturn400WhenDateAndSectorMissing() throws Exception {
		mockMvc.perform(get("/revenue"))
				.andExpect(status().isBadRequest());
	}

}
