package com.estapar.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "garage_sector")
@Getter
@Setter
@NoArgsConstructor
public class GarageSector {

	@Id
	private String sector;

	@Column(name = "base_price", nullable = false)
	private BigDecimal basePrice;

	@Column(name = "max_capacity", nullable = false)
	private int maxCapacity;

}
