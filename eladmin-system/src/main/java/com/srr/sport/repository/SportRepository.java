package com.srr.sport.repository;

import com.srr.sport.domain.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
* @author Chanheng
* @date 2025-05-17
**/
public interface SportRepository extends JpaRepository<Sport, Long>, JpaSpecificationExecutor<Sport> {

    Optional<Sport> findByName(String name);
}