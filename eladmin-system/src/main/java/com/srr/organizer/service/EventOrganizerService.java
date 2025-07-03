package com.srr.organizer.service;

import com.srr.club.domain.Club;
import com.srr.club.dto.ClubDto;
import com.srr.club.dto.ClubMapper;
import com.srr.club.repository.ClubRepository;
import com.srr.enumeration.VerificationStatus;
import com.srr.organizer.domain.EventOrganizer;
import com.srr.organizer.dto.EventOrganizerDto;
import com.srr.organizer.dto.EventOrganizerMapper;
import com.srr.organizer.dto.EventOrganizerQueryCriteria;
import com.srr.organizer.repository.EventOrganizerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.modules.security.service.enums.UserType;
import me.zhengjie.modules.system.domain.User;
import me.zhengjie.modules.system.service.UserService;
import me.zhengjie.modules.system.service.dto.UserDto;
import me.zhengjie.modules.system.service.mapstruct.UserMapper;
import me.zhengjie.utils.ExecutionResult;
import me.zhengjie.utils.QueryHelp;
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
public class EventOrganizerService {

    private final EventOrganizerRepository eventOrganizerRepository;
    private final ClubRepository clubRepository;
    private final UserMapper userMapper;
    private final EventOrganizerMapper eventOrganizerMapper;
    private final ClubMapper clubMapper;
    private final UserService userService;

    public EventOrganizer findCurrentUserEventOrganizer() {
        final User user = userService.findCurrentUser();

        if (user.getUserType() != UserType.ORGANIZER) {
            return null;
        }

        return eventOrganizerRepository.findFirstByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException(EventOrganizer.class, "userId", user.getId()));
    }

    public List<EventOrganizerDto> findEventOrganizersForOtherClubs(EventOrganizerQueryCriteria criteria, Pageable pageable) {
        // check if the club exists
        final Club club = clubRepository.findById(criteria.getClubId()).orElse(null);
        if (club == null) {
            throw new EntityNotFoundException(Club.class, "id", criteria.getClubId());
        }

        // find the event organizers by pageable and criteria
        final List<EventOrganizer> organizers = eventOrganizerRepository
                .findAll((root, query, builder) -> QueryHelp.getPredicate(root, criteria, builder), pageable).getContent();

        // map the event organizers to event organizer dto
        return organizers.stream()
                .map(eventOrganizer -> {
                    final ClubDto clubDto = clubMapper.toDto(club);
                    final UserDto userDto = userMapper.toDto(eventOrganizer.getUser());
                    final EventOrganizerDto eventOrganizerDto = eventOrganizerMapper.toDto(eventOrganizer);
                    eventOrganizerDto.setClub(clubDto);
                    eventOrganizerDto.setUser(userDto);
                    return eventOrganizerDto;
                }).toList();
    }

    @Transactional
    public ExecutionResult create(EventOrganizer resources) {
        EventOrganizer savedOrganizer = eventOrganizerRepository.save(resources);
        return ExecutionResult.of(savedOrganizer.getId());
    }


    public List<EventOrganizer> findByUserId(Long userId) {
        return eventOrganizerRepository.findByUserId(userId);
    }


    public Page<EventOrganizer> findAll(Pageable pageable) {
        return eventOrganizerRepository.findAll(pageable);
    }


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


    @Transactional
    public void linkClubs(Long organizerId, List<Long> clubIds) {
        EventOrganizer organizer = eventOrganizerRepository.findById(organizerId)
                .orElseThrow(() -> new IllegalArgumentException("Organizer not found"));
        Set<Club> clubs = new HashSet<>(clubRepository.findAllById(clubIds));
        organizer.setClubs(clubs);
        eventOrganizerRepository.save(organizer);
    }


    public Set<Club> getClubs(Long organizerId) {
        EventOrganizer organizer = eventOrganizerRepository.findById(organizerId)
                .orElseThrow(() -> new IllegalArgumentException("Organizer not found"));
        return organizer.getClubs();
    }
}
