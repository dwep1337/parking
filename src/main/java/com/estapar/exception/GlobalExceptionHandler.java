package com.estapar.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(VehicleNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleVehicleNotFound(VehicleNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message(ex.getMessage()));
	}

	@ExceptionHandler(ParkingFullException.class)
	public ResponseEntity<Map<String, String>> handleParkingFull(ParkingFullException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(message(ex.getMessage()));
	}

	@ExceptionHandler({ MethodArgumentNotValidException.class, ConstraintViolationException.class,
			IllegalArgumentException.class })
	public ResponseEntity<Map<String, String>> handleValidation(Exception ex) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message(ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(message("Erro interno ao processar a requisição"));
	}

	private Map<String, String> message(String text) {
		Map<String, String> body = new HashMap<>();
		body.put("message", text);
		return body;
	}

}
