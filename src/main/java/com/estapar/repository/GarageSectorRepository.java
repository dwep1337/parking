package com.estapar.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.estapar.entity.GarageSector;

public interface GarageSectorRepository extends JpaRepository<GarageSector, String> {

	List<GarageSector> findAllByOrderBySectorAsc();

}
