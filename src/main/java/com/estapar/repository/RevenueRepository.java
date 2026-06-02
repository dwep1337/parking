package com.estapar.repository;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.estapar.entity.Revenue;

public interface RevenueRepository extends JpaRepository<Revenue, Long> {

	@Query("""
			SELECT COALESCE(SUM(r.amount), 0)
			FROM Revenue r
			WHERE r.sector = :sector
			  AND r.createdAt >= :start
			  AND r.createdAt < :end
			""")
	BigDecimal sumAmountBySectorAndPeriod(
			@Param("sector") String sector,
			@Param("start") Instant start,
			@Param("end") Instant end);

}
