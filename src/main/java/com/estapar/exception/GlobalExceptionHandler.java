package com.estapar.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(VehicleNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleVehicleNotFound(VehicleNotFoundException ex) {
		log.warn("Veículo não encontrado: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message(ex.getMessage()));
	}

	@ExceptionHandler(ParkingFullException.class)
	public ResponseEntity<Map<String, String>> handleParkingFull(ParkingFullException ex) {
		log.warn("Estacionamento lotado ou sessão duplicada: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(message(ex.getMessage()));
	}

	@ExceptionHandler(BusinessValidationException.class)
	public ResponseEntity<Map<String, String>> handleBusinessValidation(BusinessValidationException ex) {
		log.warn("Requisição inválida: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message(ex.getMessage()));
	}

	@ExceptionHandler({ MethodArgumentNotValidException.class, ConstraintViolationException.class,
			IllegalArgumentException.class })
	public ResponseEntity<Map<String, String>> handleValidation(Exception ex) {
		log.warn("Requisição inválida: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message(ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
		log.error("Erro interno: {}", ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(message("Erro interno ao processar a requisição"));
	}

	private Map<String, String> message(String text) {
		Map<String, String> body = new HashMap<>();
		body.put("message", text);
		return body;
	}

}
