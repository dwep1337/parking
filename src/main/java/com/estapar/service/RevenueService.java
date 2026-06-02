package com.estapar.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.estapar.dto.RevenueResponseDTO;
import com.estapar.repository.GarageSectorRepository;
import com.estapar.repository.RevenueRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RevenueService {

	private static final String CURRENCY = "BRL";

	private final GarageSectorRepository sectorRepository;
	private final RevenueRepository revenueRepository;

	@Transactional(readOnly = true)
	public RevenueResponseDTO getRevenue(LocalDate date, String sector) {
		if (!sectorRepository.existsById(sector)) {
			throw new IllegalArgumentException("Setor não encontrado: " + sector);
		}

		Instant start = date.atStartOfDay().toInstant(ZoneOffset.UTC);
		Instant end = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

		BigDecimal amount = revenueRepository.sumAmountBySectorAndPeriod(sector, start, end)
				.setScale(2, RoundingMode.HALF_UP);

		Instant timestamp = date.atTime(12, 0).toInstant(ZoneOffset.UTC);

		return new RevenueResponseDTO(amount, CURRENCY, timestamp);
	}

}
