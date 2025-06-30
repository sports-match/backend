package com.srr.event.service;

import com.srr.enumeration.*;
import com.srr.event.domain.Event;
import com.srr.event.domain.MatchGroup;
import com.srr.event.domain.Tag;
import com.srr.event.domain.WaitList;
import com.srr.event.dto.*;
import com.srr.event.mapper.MatchGroupMapper;
import com.srr.event.mapper.MatchMapper;
import com.srr.event.repository.*;
import com.srr.organizer.domain.EventOrganizer;
import com.srr.organizer.repository.EventOrganizerRepository;
import com.srr.player.domain.Player;
import com.srr.player.domain.Team;
import com.srr.player.domain.TeamPlayer;
import com.srr.player.repository.PlayerSportRatingRepository;
import com.srr.player.repository.TeamPlayerRepository;
import com.srr.player.repository.TeamRepository;
import com.srr.player.service.PlayerService;
import com.srr.player.service.TeamPlayerService;
import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.vo.EmailVo;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.modules.system.repository.UserRepository;
import me.zhengjie.service.EmailService;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import javax.validation.Valid;
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
public class EventService {

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
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PlayerService playerService;
    private final TeamPlayerService teamPlayerService;
    private final MatchGroupMapper matchGroupMapper;
    private final MatchMapper matchMapper;

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

    @Transactional
    public EventDto findById(Long id) {
        Event event = eventRepository.findById(id).orElseGet(Event::new);
        ValidationUtil.isNull(event.getId(), "Event", "id", id);
        return eventMapper.toDto(event);
    }

    /**
     * @param resource
     * @return
     */
    @Transactional
    public EventDto create(@Valid EventDto resource) {
        // Date/time validation
        Timestamp now = new Timestamp(System.currentTimeMillis());

        if (resource.getEventTime().before(now)) {
            throw new BadRequestException("Event time cannot be in the past.");
        }
        if (resource.getCheckInEnd() == null) {
            resource.setCheckInEnd(resource.getEventTime()); // Default to event start time
        }

        if (resource.getCheckInStart() == null) {
            resource.setCheckInStart(new Timestamp(resource.getEventTime().getTime() - 60 * 60 * 1000)); // Default to 1 hour before
        }

        if (resource.getCheckInStart() != null && resource.getCheckInStart().after(resource.getEventTime())) {
            throw new BadRequestException("Check-in start time cannot be after event time.");
        }
        // Validate check-in window
        if (resource.getCheckInStart() != null && resource.getCheckInEnd() != null && resource.getEventTime() != null) {
            if (!(resource.getCheckInStart().before(resource.getCheckInEnd()) && resource.getCheckInEnd().before(resource.getEventTime()) || resource.getCheckInEnd().equals(resource.getEventTime()))) {
                throw new BadRequestException("Check-in window must be before or at event time and start before end.");
            }
        }

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
        EventDto responseDto = eventMapper.toDto(result);
        // Generate event link with full domain
        String eventLink = "https://sportrevive.com/events/" + result.getId();
        responseDto.setPublicLink(eventLink);
        return responseDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public EventDto update(EventUpdateDto resources) {
        Event event = eventRepository.findById(resources.getId()).orElseGet(Event::new);
        ValidationUtil.isNull(event.getId(), "Event", "id", resources.getId());
        if (!(event.getStatus() == EventStatus.PUBLISHED ||
                event.getStatus() == EventStatus.CHECK_IN)) {
            throw new BadRequestException("Cannot update event with status: " + event.getStatus());
        }
        if (resources.getGroupCount() != null) {
            event.setGroupCount(resources.getGroupCount());
        }
        if (resources.getIsPublic() != null) {
            event.setIsPublic(resources.getIsPublic());
        }
        if (resources.getMaxParticipants() != null) {
            event.setMaxParticipants(resources.getMaxParticipants());
        }
        if (resources.getAllowWaitList() != null) {
            event.setAllowWaitList(resources.getAllowWaitList());
        }
        if (resources.getCheckInStart() != null) {
            event.setCheckInStart(resources.getCheckInStart());
        }
        if (resources.getCheckInEnd() != null) {
            event.setCheckInEnd(resources.getCheckInEnd());
        } else {
            // If end not provided but start is being updated, default to event time
            event.setCheckInEnd(event.getEventTime());
        }
        // Validate check-in window if either is being updated
        if (event.getCheckInStart() != null && event.getCheckInEnd() != null && event.getEventTime() != null) {
            if (!(event.getCheckInStart().before(event.getCheckInEnd()) && event.getCheckInEnd().before(event.getEventTime()) || event.getCheckInEnd().equals(event.getEventTime()))) {
                throw new BadRequestException("Check-in window must be before or at event time and start before end.");
            }
        }
        final var result = eventRepository.save(event);
        EventDto responseDto = eventMapper.toDto(result);
        String eventLink = "https://sportrevive.com/events/" + result.getId();
        responseDto.setPublicLink(eventLink);
        return responseDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public EventDto updateStatus(Long id, EventStatus status) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "id", String.valueOf(id)));

        event.setStatus(status);
        if (status == EventStatus.CHECK_IN) {
            event.setCheckInStart(Timestamp.from(Instant.now()));
        }

        final var result = eventRepository.save(event);
        EventDto responseDto = eventMapper.toDto(result);
        String eventLink = "https://sportrevive.com/events/" + result.getId();
        responseDto.setPublicLink(eventLink);
        return responseDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public EventDto joinEvent(JoinEventDto joinEventDto) {
        Long playerId = joinEventDto.getPlayerId();
        if (playerId == null) {
            throw new BadRequestException("Player ID is required to join event");
        }

        Event event = eventRepository.findById(joinEventDto.getEventId())
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "id", String.valueOf(joinEventDto.getEventId())));

        var ratingOpt = playerSportRatingRepository.findByPlayerIdAndSportIdAndFormat(playerId, event.getSportId(), Format.DOUBLE);
        if (ratingOpt.isEmpty() || ratingOpt.get().getRateScore() == null || ratingOpt.get().getRateScore() <= 0) {
            throw new BadRequestException("Please complete your self-assessment before joining an event.");
        }

        // Block joining if event is private
        if (Boolean.FALSE.equals(event.getIsPublic())) {
            throw new BadRequestException("This event is private. Joining is not allowed.");
        }

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BadRequestException("Event is not open for joining");
        }

        // Prevent duplicate registration for the same event
        if (teamPlayerRepository.findByEventId(event.getId()).stream().anyMatch(tp -> tp.getPlayer().getId().equals(playerId))) {
            throw new BadRequestException("Player is already registered for this event");
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

        // Remove teamId logic: always create a new team for the player
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
        team.setStatus(TeamStatus.REGISTERED);

        // Calculate average rating for the new team
        double avg = 0.0;
        if (ratingOpt.get().getRateScore() != null) {
            avg = ratingOpt.get().getRateScore();
        }
        team.setAverageScore(avg);

        Team savedTeam = teamRepository.save(team);

        TeamPlayer teamPlayer = new TeamPlayer();
        teamPlayer.setTeam(savedTeam);
        Player player = new Player();
        player.setId(joinEventDto.getPlayerId());
        teamPlayer.setPlayer(player);
        teamPlayer.setCheckedIn(false);
        teamPlayer.setStatus(TeamPlayerStatus.REGISTERED);
        teamPlayer.setRegistrationTime(Timestamp.from(Instant.now()));
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

        EventDto responseDto = eventMapper.toDto(event);
        responseDto.setPublicLink("https://sportrevive.com/events/" + event.getId());
        return responseDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public EventDto withdrawFromEvent(Long eventId) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "id", String.valueOf(eventId)));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BadRequestException("Withdrawal is not allowed at the current event status: " + event.getStatus());
        }

        // Check if player is on the main list
        TeamPlayer teamPlayer = teamPlayerRepository.findByEventIdAndPlayerUserId(eventId, currentUserId);
        if (teamPlayer != null) {
            teamPlayer.setStatus(TeamPlayerStatus.WITHDRAWN);
            teamPlayerRepository.save(teamPlayer);

            // Get the team and update its state
            Team team = teamPlayer.getTeam();

            // Remove the player from the team
            teamPlayerRepository.delete(teamPlayer);

            // Use new helper for team update
            teamPlayerService.updateTeamStateAndStatus(team);

            // Only decrement if player was on main list (not waitlist)
            if (event.getCurrentParticipants() != null && event.getCurrentParticipants() > 0) {
                event.setCurrentParticipants(event.getCurrentParticipants() - 1);
            }

            // Promote from waitlist if applicable
            if (event.isAllowWaitList()) {
                List<WaitList> waitList = waitListRepository.findByEventIdOrderByCreateTimeAsc(eventId);
                if (!waitList.isEmpty()) {
                    WaitList topOfWaitList = waitList.get(0);
                    // This is a simplified promotion. A full implementation would create a new team/player entry.
                    waitListRepository.delete(topOfWaitList); // Remove from waitlist
                    event.setCurrentParticipants((event.getCurrentParticipants() == null ? 0 : event.getCurrentParticipants()) + 1); // Add the promoted player
                }
            }
        } else {
            // Check if player is on the waitlist
            WaitList waitListEntry = waitListRepository.findByEventIdAndPlayerId(eventId, currentUserId);
            if (waitListEntry != null) {
                waitListRepository.delete(waitListEntry);
            } else {
                throw new BadRequestException("Player is not registered for this event or on its waitlist.");
            }
        }

        Event updatedEvent = eventRepository.save(event);
        EventDto responseDto = eventMapper.toDto(updatedEvent);
        String eventLink = "https://sportrevive.com/events/" + updatedEvent.getId();
        responseDto.setPublicLink(eventLink);
        return responseDto;
    }

    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult deleteAll(Long[] ids) {
        List<Long> successfulDeletes = new ArrayList<>();
        List<Long> failedDeletes = new ArrayList<>();

        for (Long id : ids) {
            Optional<Event> eventOptional = eventRepository.findById(id);
            if (eventOptional.isPresent()) {
                Event event = eventOptional.get();
                if (event.getStatus() == EventStatus.PUBLISHED || event.getStatus() == EventStatus.CLOSED) {
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

    @Transactional
    public ExecutionResult remind(Long id, RemindDto remindDto) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "id", String.valueOf(id)));

        List<TeamPlayer> playersToRemind = teamPlayerRepository.findByEventId(id).stream()
                .filter(tp -> !tp.isCheckedIn()).toList();

        if (remindDto != null && remindDto.getPlayerId() != null) {
            playersToRemind = playersToRemind.stream()
                    .filter(tp -> tp.getPlayer().getId().equals(remindDto.getPlayerId()))
                    .toList();
        }

        if (playersToRemind.isEmpty()) {
            return ExecutionResult.of(id, Map.of("message", "No players to remind."));
        }

        List<String> recipientEmails = playersToRemind.stream()
                .map(tp -> userRepository.findById(tp.getPlayer().getUserId()).orElse(null))
                .filter(Objects::nonNull)
                .map(me.zhengjie.modules.system.domain.User::getEmail)
                .toList();

        if (recipientEmails.isEmpty()) {
            return ExecutionResult.of(id, Map.of("message", "No valid emails found for players to remind."));
        }

        var emailConfig = emailService.find();
        if (emailConfig.getId() == null) {
            throw new BadRequestException("Please configure email settings first.");
        }

        String subject = "Reminder: Check-in for event " + event.getName();
        String content = "<p>Hi,</p>" +
                "<p>This is a reminder to check in for the event: <strong>" + event.getName() + "</strong>.</p>" +
                "<p>The event is scheduled for: " + event.getEventTime() + ". Please make sure to check in on time.</p>" +
                "<p>Thank you!</p>";

        EmailVo emailVo = new EmailVo(recipientEmails, subject, content);
        emailService.send(emailVo, emailConfig);

        return ExecutionResult.of(id, Map.of("remindersSent", recipientEmails.size()));
    }

    @Transactional(readOnly = true)
    public List<MatchGroupDto> findGroup(Long eventId) {
        final var matchGroup = matchGroupRepository.findAllByEventId(eventId);
        return matchGroupMapper.toDto(matchGroup);
    }

    /**
     * Get event results: groups and their matches for a given event
     */
    public List<MatchGroupDto> getEventResults(Long eventId) {
        var groups = matchGroupRepository.findAllByEventId(eventId);
        return groups.stream().map(group -> {
            var groupDto = matchGroupMapper.toDto(group);
            var matches = matchRepository.findAllByMatchGroupId(group.getId())
                .stream()
                .map(match -> {
                    return matchMapper.toDto(match);
                })
                .toList();
            groupDto.setMatches(matches);
            return groupDto;
        }).toList();
    }
}