package com.srr.club.repository;

import com.srr.club.domain.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
* @author Chanheng
* @date 2025-05-18
**/
public interface ClubRepository extends JpaRepository<Club, Long>, JpaSpecificationExecutor<Club> {
}