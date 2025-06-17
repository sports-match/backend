package com.srr.organizer.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
* @author Chanheng
* @date 2025-05-26
**/
@Repository
public interface EventOrganizerRepository extends JpaRepository<EventOrganizer, Long>, JpaSpecificationExecutor<EventOrganizer> {
    
    /**
     * Find event organizers by user id
     * @param userId the user id
     * @return list of event organizers
     */
    List<EventOrganizer> findByUserId(Long userId);

    Optional<EventOrganizer> findFirstByUserId(Long userId);
}
