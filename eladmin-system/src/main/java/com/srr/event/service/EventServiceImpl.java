package com.srr.event.service;

import com.srr.domain.*;
import com.srr.event.domain.*;
import com.srr.event.dto.EventDto;
import com.srr.dto.EventQueryCriteria;
import com.srr.event.dto.JoinEventDto;
import com.srr.dto.RemindDto;
import com.srr.dto.enums.EventTimeFilter;
import com.srr.event.dto.EventMapper;
import com.srr.enumeration.EventStatus;
import com.srr.enumeration.Format;
import com.srr.enumeration.VerificationStatus;
import com.srr.organizer.domain.EventOrganizer;
import com.srr.organizer.domain.EventOrganizerRepository;
import com.srr.repository.*;
import lombok.RequiredArgsConstructor;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

/**
 * @author Chanheng
 * @description 服务实现
 * @date 2025-05-18
 **/
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final TeamRepository teamRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final MatchGroupRepository matchGroupRepository;
    private final MatchRepository matchRepository;
    private final WaitListRepository waitListRepository;
    private final PlayerSportRatingRepository playerSportRatingRepository;
    private final EventOrganizerRepository eventOrganizerRepository;
    private final TagRepository tagRepository;

    private Set<Tag> processIncomingTags(Set<String> tagsFromResource) {
        if (tagsFromResource == null || tagsFromResource.isEmpty()) {
            return new HashSet<>();
        }
        Set<Tag> managedTags = new HashSet<>();
        for (String inputTag : tagsFromResource) {
            if (inputTag != null && !inputTag.trim().isEmpty()) {
                String tagName = inputTag.trim();
                Tag persistentTag = tagRepository.findByName(tagName)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            return newTag;
                        });
                managedTags.add(persistentTag);
            }
        }
        return managedTags;
    }

    @Override
    public PageResult<EventDto> queryAll(EventQueryCriteria criteria, Pageable pageable) {
        Page<Event> page = eventRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            Predicate predicate = QueryHelp.getPredicate(root, criteria, criteriaBuilder);
            if (criteria.getEventTimeFilter() != null) {
                Timestamp now = new Timestamp(System.currentTimeMillis());
                if (criteria.getEventTimeFilter() == EventTimeFilter.UPCOMING) {
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("eventTime"), now));
                } else if (criteria.getEventTimeFilter() == EventTimeFilter.PAST) {
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThan(root.get("eventTime"), now));
                }
            }
            return predicate;
        }, pageable);
        return PageUtil.toPage(page.map(eventMapper::toDto));
    }

    @Override
    public List<EventDto> queryAll(EventQueryCriteria criteria) {
        return eventMapper.toDto(eventRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            Predicate predicate = QueryHelp.getPredicate(root, criteria, criteriaBuilder);
            if (criteria.getEventTimeFilter() != null) {
                Timestamp now = new Timestamp(System.currentTimeMillis());
                if (criteria.getEventTimeFilter() == EventTimeFilter.UPCOMING) {
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("eventTime"), now));
                } else if (criteria.getEventTimeFilter() == EventTimeFilter.PAST) {
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.lessThan(root.get("eventTime"), now));
                }
            }
            return predicate;
        }));
    }

    @Override
    @Transactional
    public EventDto findById(Long id) {
        Event event = eventRepository.findById(id).orElseGet(Event::new);
        ValidationUtil.isNull(event.getId(), "Event", "id", id);
        return eventMapper.toDto(event);
    }

    @Override
    @Transactional
    public EventDto create(EventDto resource) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        var event = eventMapper.toEntity(resource);

        Optional<EventOrganizer> organizerList = eventOrganizerRepository.findFirstByUserId(currentUserId);
        if (organizerList.isPresent()) {
            EventOrganizer organizer = organizerList.get();
            if (organizer.getVerificationStatus() != VerificationStatus.VERIFIED) {
                throw new BadRequestException("Organizer account is not verified. Event creation is not allowed.");
            }
            if (resource.getClubId() != null) {
                // club must be linked to the organizer
                validateOrganizerClubPermission(organizer, resource.getClubId());
            }
            event.setOrganizer(organizer);
        }

        // Set the creator of the event using the Long ID directly
        if (resource.getCreateBy() == null) { // Event.java has 'createBy' as Long
            event.setCreateBy(currentUserId);
        }
        // If organizerList is empty, it means the user is not an organizer (e.g., an admin),
        // so the check is bypassed. Permission to create is handled by @PreAuthorize.

        event.setStatus(EventStatus.PUBLISHED);
        Set<Tag> processedTags = processIncomingTags(resource.getTags());
        event.setTags(processedTags);

        final var result = eventRepository.save(event);
        return eventMapper.toDto(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EventDto update(Event resources) {
        Event event = eventRepository.findById(resources.getId()).orElseGet(Event::new);
        ValidationUtil.isNull(event.getId(), "Event", "id", resources.getId());
        // Add status validation: only allow update if not PUBLISHED, CHECK_IN, IN_PROGRESS, CLOSED, or DELETED
        if (!(event.getStatus() == EventStatus.PUBLISHED ||
                event.getStatus() == EventStatus.CHECK_IN)) {
            throw new BadRequestException("Cannot update event with status: " + event.getStatus());
        }
        event.copy(resources);
        final var result = eventRepository.save(event);
        return eventMapper.toDto(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EventDto updateStatus(Long id, EventStatus status) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "id", String.valueOf(id)));

        event.setStatus(status);
        if (status == EventStatus.CHECK_IN) {
            event.setCheckInAt(Timestamp.from(Instant.now()));
        }

        final var result = eventRepository.save(event);
        return eventMapper.toDto(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public EventDto joinEvent(JoinEventDto joinEventDto) {
        Long playerId = joinEventDto.getPlayerId();
        if (playerId == null) {
            throw new BadRequestException("Player ID is required to join event");
        }
        var ratingOpt = playerSportRatingRepository.findByPlayerIdAndSportAndFormat(playerId, "Badminton", "DOUBLES");
        if (ratingOpt.isEmpty() || ratingOpt.get().getRateScore() == null || ratingOpt.get().getRateScore() <= 0) {
            throw new BadRequestException("Please complete your self-assessment before joining an event.");
        }

        Event event = eventRepository.findById(joinEventDto.getEventId())
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "id", String.valueOf(joinEventDto.getEventId())));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BadRequestException("Event is not open for joining");
        }

        boolean isWaitList = joinEventDto.getJoinWaitList() != null && joinEventDto.getJoinWaitList();
        if (event.getMaxParticipants() != null &&
                (event.getCurrentParticipants() != null && event.getCurrentParticipants() >= event.getMaxParticipants()) &&
                !isWaitList) {
            if (!event.isAllowWaitList()) {
                throw new BadRequestException("Event is full and does not allow waitlist");
            }
            isWaitList = true;
        }

        if (joinEventDto.getTeamId() != null) {
            Team team = teamRepository.findById(joinEventDto.getTeamId())
                    .orElseThrow(() -> new EntityNotFoundException(Team.class, "id", String.valueOf(joinEventDto.getTeamId())));

            if (!team.getEvent().getId().equals(event.getId())) {
                throw new BadRequestException("Team does not belong to this event");
            }

            if (teamPlayerRepository.existsByTeamIdAndPlayerId(team.getId(), joinEventDto.getPlayerId())) {
                throw new BadRequestException("Player is already in this team");
            }

            if (team.getTeamPlayers().size() >= team.getTeamSize()) {
                throw new BadRequestException("Team is already full");
            }

            TeamPlayer teamPlayer = new TeamPlayer();
            teamPlayer.setTeam(team);
            Player player = new Player();
            player.setId(joinEventDto.getPlayerId());
            teamPlayer.setPlayer(player);
            teamPlayer.setCheckedIn(false);
            teamPlayerRepository.save(teamPlayer);

            teamRepository.save(team);
        } else {
            Team team = new Team();
            team.setEvent(event);

            if (event.getFormat() == Format.SINGLE) {
                team.setName("Player " + joinEventDto.getPlayerId());
                team.setTeamSize(1);
            } else if (event.getFormat() == Format.DOUBLE) {
                team.setName("New Team");
                team.setTeamSize(2);
            } else {
                team.setName("New Team");
                team.setTeamSize(4);
            }

            Team savedTeam = teamRepository.save(team);

            TeamPlayer teamPlayer = new TeamPlayer();
            teamPlayer.setTeam(savedTeam);
            Player player = new Player();
            player.setId(joinEventDto.getPlayerId());
            teamPlayer.setPlayer(player);
            teamPlayer.setCheckedIn(false);
            teamPlayerRepository.save(teamPlayer);

            if (!isWaitList) {
                event.setCurrentParticipants((event.getCurrentParticipants() == null ? 0 : event.getCurrentParticipants()) + 1);
            } else {
                WaitList waitListEntry = new WaitList();
                waitListEntry.setEventId(event.getId());
                Player waitingPlayer = new Player();
                waitingPlayer.setId(joinEventDto.getPlayerId());
                waitListEntry.setPlayerId(waitingPlayer.getId());
                waitListRepository.save(waitListEntry);
            }
        }

        eventRepository.save(event);
        return eventMapper.toDto(event);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult deleteAll(Long[] ids) {
        List<Long> successfulDeletes = new ArrayList<>();
        List<Long> failedDeletes = new ArrayList<>();

        for (Long id : ids) {
            Optional<Event> eventOptional = eventRepository.findById(id);
            if (eventOptional.isPresent()) {
                Event event = eventOptional.get();
                if (event.getStatus() == EventStatus.DRAFT || event.getStatus() == EventStatus.CLOSED) {
                    List<MatchGroup> matchGroups = matchGroupRepository.findAllByEventId(id);
                    for (MatchGroup group : matchGroups) {
                        matchRepository.deleteByMatchGroupId(group.getId());
                    }
                    matchGroupRepository.deleteByEventId(id);
                    teamPlayerRepository.deleteByTeamEventId(id);
                    teamRepository.deleteByEventId(id);
                    waitListRepository.deleteByEventId(id);
                    eventRepository.deleteById(id);
                    successfulDeletes.add(id);
                } else {
                    failedDeletes.add(id);
                }
            } else {
                failedDeletes.add(id);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("successfulDeletes", successfulDeletes.size());
        data.put("failedDeletes", failedDeletes.size());
        data.put("details", Map.of("successfulIds", successfulDeletes, "failedIds", failedDeletes));

        Long operationId = (ids != null && ids.length > 0) ? ids[0] : null;
        if (!failedDeletes.isEmpty()) {
            operationId = failedDeletes.get(0);
        }
        return ExecutionResult.of(operationId, data);
    }

    private void validateOrganizerClubPermission(EventOrganizer organizer, Long clubId) {
        boolean allowed = organizer
                .getClubs()
                .stream()
                .anyMatch(club -> club.getId().equals(clubId));
        if (!allowed) {
            throw new org.springframework.security.access.AccessDeniedException("Organizer is not allowed to manage this club");
        }
    }

    @Override
    public ExecutionResult remind(Long id, RemindDto remindDto) {
        return null;
    }
}