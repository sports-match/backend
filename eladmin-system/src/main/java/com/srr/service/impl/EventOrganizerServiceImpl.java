package com.srr.service.impl;

import com.srr.club.domain.Club;
import com.srr.organizer.domain.EventOrganizer;
import com.srr.enumeration.VerificationStatus;
import com.srr.club.domain.ClubRepository;
import com.srr.organizer.domain.EventOrganizerRepository;
import com.srr.service.EventOrganizerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.utils.ExecutionResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of EventOrganizerService for managing event organizers
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventOrganizerServiceImpl implements EventOrganizerService {

    private final EventOrganizerRepository eventOrganizerRepository;
    private final ClubRepository clubRepository;

    @Override
    @Transactional
    public ExecutionResult create(EventOrganizer resources) {
        EventOrganizer savedOrganizer = eventOrganizerRepository.save(resources);
        return ExecutionResult.of(savedOrganizer.getId());
    }

    @Override
    public List<EventOrganizer> findByUserId(Long userId) {
        return eventOrganizerRepository.findByUserId(userId);
    }

    @Override
    public Page<EventOrganizer> findAll(Pageable pageable) {
        return eventOrganizerRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public ExecutionResult updateVerificationStatus(Long organizerId, VerificationStatus status) {
        log.info("Updating verification status for organizer ID: {} to {}", organizerId, status);
        EventOrganizer organizer = eventOrganizerRepository.findById(organizerId)
                .orElseThrow(() -> new EntityNotFoundException(EventOrganizer.class, "id", organizerId));

        organizer.setVerificationStatus(status);
        EventOrganizer updatedOrganizer = eventOrganizerRepository.save(organizer);
        log.info("Successfully updated verification status for organizer ID: {}", updatedOrganizer.getId());
        return ExecutionResult.of(updatedOrganizer.getId());
    }

    @Override
    @Transactional
    public void linkClubs(Long organizerId, List<Long> clubIds) {
        EventOrganizer organizer = eventOrganizerRepository.findById(organizerId)
                .orElseThrow(() -> new IllegalArgumentException("Organizer not found"));
        Set<Club> clubs = new HashSet<>(clubRepository.findAllById(clubIds));
        organizer.setClubs(clubs);
        eventOrganizerRepository.save(organizer);
    }

    @Override
    public Set<Club> getClubs(Long organizerId) {
        EventOrganizer organizer = eventOrganizerRepository.findById(organizerId)
                .orElseThrow(() -> new IllegalArgumentException("Organizer not found"));
        return organizer.getClubs();
    }
}
