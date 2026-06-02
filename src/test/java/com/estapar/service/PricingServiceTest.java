package com.estapar.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PricingServiceTest {

	private PricingService pricingService;

	@BeforeEach
	void setUp() {
		pricingService = new PricingService();
	}

	@Test
	void shouldApplyTenPercentDiscountWhenOccupancyBelow25() {
		BigDecimal rate = pricingService.calculateHourlyRate(new BigDecimal("10.00"), 0.10);
		assertEquals(new BigDecimal("9.00"), rate);
	}

	@Test
	void shouldKeepNormalPriceWhenOccupancyUpTo50() {
		BigDecimal rate = pricingService.calculateHourlyRate(new BigDecimal("10.00"), 0.40);
		assertEquals(new BigDecimal("10.00"), rate);
	}

	@Test
	void shouldApplyTenPercentIncreaseWhenOccupancyUpTo75() {
		BigDecimal rate = pricingService.calculateHourlyRate(new BigDecimal("10.00"), 0.60);
		assertEquals(new BigDecimal("11.00"), rate);
	}

	@Test
	void shouldApplyTwentyFivePercentIncreaseWhenOccupancyAbove75() {
		BigDecimal rate = pricingService.calculateHourlyRate(new BigDecimal("10.00"), 0.90);
		assertEquals(new BigDecimal("12.50"), rate);
	}

	@Test
	void shouldKeepNormalPriceAt25PercentOccupancy() {
		BigDecimal rate = pricingService.calculateHourlyRate(new BigDecimal("10.00"), 0.25);
		assertEquals(new BigDecimal("10.00"), rate);
	}

	@Test
	void shouldKeepNormalPriceAt50PercentOccupancy() {
		BigDecimal rate = pricingService.calculateHourlyRate(new BigDecimal("10.00"), 0.50);
		assertEquals(new BigDecimal("10.00"), rate);
	}

	@Test
	void shouldApplyTenPercentIncreaseAt75PercentOccupancy() {
		BigDecimal rate = pricingService.calculateHourlyRate(new BigDecimal("10.00"), 0.75);
		assertEquals(new BigDecimal("11.00"), rate);
	}

	@Test
	void shouldApplyTwentyFivePercentIncreaseAt100PercentOccupancy() {
		BigDecimal rate = pricingService.calculateHourlyRate(new BigDecimal("10.00"), 1.0);
		assertEquals(new BigDecimal("12.50"), rate);
	}

	@Test
	void shouldChargeZeroWhenStayWithin30Minutes() {
		BigDecimal fee = pricingService.calculateParkingFee(new BigDecimal("10.00"), 30);
		assertEquals(new BigDecimal("0.00"), fee);
	}

	@Test
	void shouldChargeOneHourWhenStay31Minutes() {
		BigDecimal fee = pricingService.calculateParkingFee(new BigDecimal("10.00"), 31);
		assertEquals(new BigDecimal("10.00"), fee);
	}

	@Test
	void shouldChargeOneHourWhen90BillableMinutesAfterFreePeriod() {
		BigDecimal fee = pricingService.calculateParkingFee(new BigDecimal("10.00"), 90);
		assertEquals(new BigDecimal("10.00"), fee);
	}

	@Test
	void shouldChargeTwoHoursWhen91BillableMinutesAfterFreePeriod() {
		BigDecimal fee = pricingService.calculateParkingFee(new BigDecimal("10.00"), 121);
		assertEquals(new BigDecimal("20.00"), fee);
	}

}
