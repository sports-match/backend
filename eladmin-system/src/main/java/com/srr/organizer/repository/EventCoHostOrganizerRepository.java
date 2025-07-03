package com.srr.organizer.repository;

import com.srr.organizer.domain.EventCoHostOrganizer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Cheyleap
 * @date 2025-07-03
 **/
@Repository
public interface EventCoHostOrganizerRepository extends JpaRepository<EventCoHostOrganizer, Long> {
}
