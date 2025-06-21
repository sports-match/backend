package com.srr.event.service;

import com.srr.enumeration.EventStatus;
import com.srr.enumeration.EventTimeFilter;
import com.srr.enumeration.Format;
import com.srr.enumeration.TeamPlayerStatus;
import com.srr.enumeration.TeamStatus;
import com.srr.enumeration.VerificationStatus;
import com.srr.event.domain.Event;
import com.srr.event.domain.MatchGroup;
import com.srr.event.domain.Tag;
import com.srr.event.domain.WaitList;
import com.srr.event.dto.*;
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
import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.vo.EmailVo;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.service.EmailService;
import me.zhengjie.modules.system.repository.UserRepository;
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

    /**
     * 
     *
     * @param criteria 
     * @param pageable 
     * @return 
     */
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

    /**
     * 
     *
     * @param criteria 
     * @return 
     */
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

    /**
     * 
     *
     * @param id 
     * @return 
     */
    @Transactional
    public EventDto findById(Long id) {
        Event event = eventRepository.findById(id).orElseGet(Event::new);
        ValidationUtil.isNull(event.getId(), "Event", "id", id);
        return eventMapper.toDto(event);
    }

    /**
     * 
     *
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

    /**
     * 
     *
     * @param resources 
     * @return 
     */
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
            if (!(event.getCheckInStart().before(event.getCheckInEnd()) && (event.getCheckInEnd().before(event.getEventTime()) || event.getCheckInEnd().equals(event.getEventTime())))) {
                throw new BadRequestException("Check-in window must be before or at event time and start before end.");
            }
        }
        final var result = eventRepository.save(event);
        EventDto responseDto = eventMapper.toDto(result);
        String eventLink = "https://sportrevive.com/events/" + result.getId();
        responseDto.setPublicLink(eventLink);
        return responseDto;
    }

    /**
     * 
     *
     * @param id 
     * @param status 
     * @return 
     */
    @Transactional(rollbackFor = Exception.class)
    public EventDto updateStatus(Long id, EventStatus status) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "id", String.valueOf(id)));

        event.setStatus(status);
        if (status == EventStatus.CHECK_IN) {
            event.setCheckInAt(Timestamp.from(Instant.now()));
        }

        final var result = eventRepository.save(event);
        EventDto responseDto = eventMapper.toDto(result);
        String eventLink = "https://sportrevive.com/events/" + result.getId();
        responseDto.setPublicLink(eventLink);
        return responseDto;
    }

    /**
     * Merge two teams if players from different teams want to join together for the same event.
     * Ensures both players end up in only one team and cleans up the unused team.
     */
    /**
     * Merge teams if needed and return true if player is already in target team after merge.
     */
    private boolean ensurePlayerInTargetTeam(Long eventId, Long joiningPlayerId, Long targetTeamId) {
        // Find the joining player's existing TeamPlayer for this event
        TeamPlayer joiningTeamPlayer = teamPlayerRepository.findByEventId(eventId).stream()
            .filter(tp -> tp.getPlayer().getId().equals(joiningPlayerId))
            .findFirst().orElse(null);
        if (joiningTeamPlayer == null) {
            return false; // player not in any team yet
        }
        Team oldTeam = joiningTeamPlayer.getTeam();
        if (oldTeam.getId().equals(targetTeamId)) {
            return true; // already in target
        }

        // Prevent merge if target team is full
        Team targetTeam = teamRepository.findById(targetTeamId)
            .orElseThrow(() -> new EntityNotFoundException(Team.class, "id", String.valueOf(targetTeamId)));
        if (targetTeam.getTeamPlayers().size() >= targetTeam.getTeamSize()) {
            throw new BadRequestException("Target team is already full.");
        }
        if (targetTeam.getStatus() == TeamStatus.WITHDRAWN) {
            throw new BadRequestException("Cannot join a withdrawn team.");
        }

        // Prevent merge if player status withdrawn
        if (joiningTeamPlayer.getStatus() == TeamPlayerStatus.WITHDRAWN) {
            throw new BadRequestException("Withdrawn player cannot join a team.");
        }

        // Move the player to the target team
        joiningTeamPlayer.setTeam(targetTeam);
        teamPlayerRepository.save(joiningTeamPlayer);

        // Update team timestamp
        targetTeam.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        teamRepository.save(targetTeam);

        // If old team is now empty, delete it
        if (teamPlayerRepository.findAllByTeamId(oldTeam.getId()).isEmpty()) {
            teamRepository.deleteById(oldTeam.getId());
        }
        return true;
    }

    /**
     * 
     *
     * @param joinEventDto 
     * @return 
     */
    @Transactional(rollbackFor = Exception.class)
    public EventDto joinEvent(JoinEventDto joinEventDto) {
        Long playerId = joinEventDto.getPlayerId();
        if (playerId == null) {
            throw new BadRequestException("Player ID is required to join event");
        }
        final var playerDto = playerService.findById(playerId);
        var ratingOpt = playerSportRatingRepository.findByPlayerIdAndSportAndFormat(playerId, "Badminton", "DOUBLES");
        if (ratingOpt.isEmpty() || ratingOpt.get().getRateScore() == null || ratingOpt.get().getRateScore() <= 0) {
            throw new BadRequestException("Please complete your self-assessment before joining an event.");
        }

        Event event = eventRepository.findById(joinEventDto.getEventId())
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "id", String.valueOf(joinEventDto.getEventId())));

        // Block joining if event is private
        if (Boolean.FALSE.equals(event.getIsPublic())) {
            throw new BadRequestException("This event is private. Joining is not allowed.");
        }

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
            boolean alreadyInTeam = ensurePlayerInTargetTeam(joinEventDto.getEventId(), joinEventDto.getPlayerId(), joinEventDto.getTeamId());

            if (alreadyInTeam) {
                // Player already part of the team after merge, simply return event response.
                EventDto responseDto = eventMapper.toDto(event);
                responseDto.setPublicLink("https://sportrevive.com/events/" + event.getId());
                return responseDto;
            }

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
            teamPlayer.setStatus(TeamPlayerStatus.REGISTERED);
            teamPlayer.setRegistrationTime(new Timestamp(System.currentTimeMillis()));
            teamPlayerRepository.save(teamPlayer);

            teamRepository.save(team);

            if (!isWaitList) {
                event.setCurrentParticipants((event.getCurrentParticipants() == null ? 0 : event.getCurrentParticipants()) + 1);
            }
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
            team.setStatus(TeamStatus.REGISTERED);
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
        }

        eventRepository.save(event);
        EventDto responseDto = eventMapper.toDto(event);
        String eventLink = "https://sportrevive.com/events/" + event.getId();
        responseDto.setPublicLink(eventLink);
        return responseDto;
    }

    /**
     * 
     *
     * @param eventId 
     * @return 
     */
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

            // Check if all team players are withdrawn, then set team status
            Team team = teamPlayer.getTeam();
            boolean allWithdrawn = team.getTeamPlayers().stream().allMatch(tp -> tp.getStatus() == TeamPlayerStatus.WITHDRAWN);
            if (allWithdrawn) {
                team.setStatus(TeamStatus.WITHDRAWN);
                teamRepository.save(team);
            }

            TeamPlayer teamPlayer1 = teamPlayerRepository.findByEventIdAndPlayerUserId(eventId, currentUserId);
            teamPlayerRepository.delete(teamPlayer1);

            // If the team becomes empty after withdrawal, delete it
            if (team.getTeamPlayers().size() == 1) { // 1 because the player is about to be removed
                teamRepository.delete(team);
            }

            event.setCurrentParticipants(event.getCurrentParticipants() - 1);

            // Promote from waitlist if applicable
            if (event.isAllowWaitList()) {
                List<WaitList> waitList = waitListRepository.findByEventIdOrderByCreateTimeAsc(eventId);
                if (!waitList.isEmpty()) {
                    WaitList topOfWaitList = waitList.get(0);
                    // This is a simplified promotion. A full implementation would create a new team/player entry.
                    // For now, we just open up a spot.
                    // A more complete version would call a new method like `promotePlayerFromWaitlist(topOfWaitList)`
                    waitListRepository.delete(topOfWaitList); // Simplified: remove from waitlist
                    event.setCurrentParticipants(event.getCurrentParticipants() + 1); // Add the promoted player
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

    /**
     * 
     *
     * @param ids 
     * @return 
     */
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
}