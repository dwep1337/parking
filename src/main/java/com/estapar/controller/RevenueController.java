package com.estapar.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.estapar.dto.RevenueRequestDTO;
import com.estapar.dto.RevenueResponseDTO;
import com.estapar.service.RevenueService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RevenueController {

	private final RevenueService revenueService;

	@GetMapping("/revenue")
	public ResponseEntity<RevenueResponseDTO> getRevenue(
			@RequestBody(required = false) RevenueRequestDTO body,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam(required = false) String sector) {
		LocalDate resolvedDate = body != null && body.date() != null ? body.date() : date;
		String resolvedSector = body != null && body.sector() != null ? body.sector() : sector;

		if (resolvedDate == null || resolvedSector == null || resolvedSector.isBlank()) {
			throw new IllegalArgumentException("Parâmetros date e sector são obrigatórios");
		}

		return ResponseEntity.ok(revenueService.getRevenue(resolvedDate, resolvedSector));
	}

}
