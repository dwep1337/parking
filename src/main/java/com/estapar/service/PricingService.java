package com.estapar.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

@Service
public class PricingService {

	private static final int FREE_MINUTES = 30;

	private static final BigDecimal DISCOUNT_MULTIPLIER = new BigDecimal("0.90");
	private static final BigDecimal NORMAL_MULTIPLIER = BigDecimal.ONE;
	private static final BigDecimal HIGH_MULTIPLIER = new BigDecimal("1.10");
	private static final BigDecimal FULL_MULTIPLIER = new BigDecimal("1.25");

	public double calculateOccupancy(long occupiedSpots, int maxCapacity) {
		if (maxCapacity <= 0) {
			return 1.0;
		}
		return (double) occupiedSpots / maxCapacity;
	}

	public BigDecimal calculateHourlyRate(BigDecimal basePrice, double occupancy) {
		BigDecimal multiplier = resolveMultiplier(occupancy);
		return basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
	}

	public BigDecimal resolveMultiplier(double occupancy) {
		if (occupancy < 0.25) {
			return DISCOUNT_MULTIPLIER;
		}
		if (occupancy <= 0.50) {
			return NORMAL_MULTIPLIER;
		}
		if (occupancy <= 0.75) {
			return HIGH_MULTIPLIER;
		}
		return FULL_MULTIPLIER;
	}

	public BigDecimal calculateParkingFee(BigDecimal hourlyRate, long durationMinutes) {
		if (durationMinutes <= FREE_MINUTES) {
			return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
		}

		long billableMinutes = durationMinutes - FREE_MINUTES;
		long billableHours = (billableMinutes + 59) / 60;

		return hourlyRate
				.multiply(BigDecimal.valueOf(billableHours))
				.setScale(2, RoundingMode.HALF_UP);
	}

}
