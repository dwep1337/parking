package com.estapar.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.estapar.repository.GarageSectorRepository;
import com.estapar.repository.RevenueRepository;

@ExtendWith(MockitoExtension.class)
class RevenueServiceTest {

	@Mock
	private GarageSectorRepository sectorRepository;

	@Mock
	private RevenueRepository revenueRepository;

	@InjectMocks
	private RevenueService revenueService;

	private LocalDate date;

	@BeforeEach
	void setUp() {
		date = LocalDate.of(2025, 1, 1);
	}

	@Test
	void shouldSumRevenueBySectorAndDate() {
		when(sectorRepository.existsById("A")).thenReturn(true);
		when(revenueRepository.sumAmountBySectorAndPeriod(
				eq("A"),
				eq(Instant.parse("2025-01-01T00:00:00.000Z")),
				eq(Instant.parse("2025-01-02T00:00:00.000Z"))))
				.thenReturn(new BigDecimal("45.50"));

		var response = revenueService.getRevenue(date, "A");

		assertEquals(new BigDecimal("45.50"), response.amount());
		assertEquals("BRL", response.currency());
		assertEquals(Instant.parse("2025-01-01T12:00:00.000Z"), response.timestamp());
	}

	@Test
	void shouldReturnZeroWhenNoRevenueRecords() {
		when(sectorRepository.existsById("B")).thenReturn(true);
		when(revenueRepository.sumAmountBySectorAndPeriod(eq("B"), eq(Instant.parse("2025-01-01T00:00:00.000Z")),
				eq(Instant.parse("2025-01-02T00:00:00.000Z"))))
				.thenReturn(BigDecimal.ZERO);

		var response = revenueService.getRevenue(date, "B");

		assertEquals(new BigDecimal("0.00"), response.amount());
	}

}
