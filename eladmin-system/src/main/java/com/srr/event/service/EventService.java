package com.srr.event.service;

import com.srr.club.domain.Club;
import com.srr.club.service.ClubService;
import com.srr.enumeration.*;
import com.srr.event.domain.Event;
import com.srr.event.domain.MatchGroup;
import com.srr.event.domain.Tag;
import com.srr.event.domain.WaitList;
import com.srr.event.dto.*;
import com.srr.event.mapper.MatchGroupMapper;
import com.srr.event.mapper.MatchMapper;
import com.srr.event.repository.*;
import com.srr.organizer.domain.EventCoHostOrganizer;
import com.srr.organizer.domain.EventOrganizer;
import com.srr.organizer.repository.EventOrganizerRepository;
import com.srr.organizer.service.EventCoHostOrganizerService;
import com.srr.organizer.service.EventOrganizerService;
import com.srr.player.domain.Player;
import com.srr.player.domain.Team;
import com.srr.player.domain.TeamPlayer;
import com.srr.player.repository.PlayerSportRatingRepository;
import com.srr.player.repository.TeamPlayerRepository;
import com.srr.player.repository.TeamRepository;
import com.srr.player.service.TeamPlayerService;
import lombok.RequiredArgsConstructor;
import me.zhengjie.domain.EmailConfig;
import me.zhengjie.domain.vo.EmailVo;
import me.zhengjie.exception.BadRequestException;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.modules.system.repository.UserRepository;
import me.zhengjie.service.EmailService;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Chanheng
 * @description 服务实现
 * @date 2025-05-18
 **/
@Service
@RequiredArgsConstructor
public class EventService {
    private static final Map<Format, Integer> EVENT_FORMAT_PLAYERS = Map.of(
            Format.SINGLE, 2,
            Format.DOUBLE, 4);
    private static final String EVENT_BASE_URL = "https://sportrevive.com/events/";
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
    private final TeamPlayerService teamPlayerService;
    private final MatchGroupMapper matchGroupMapper;
    private final MatchMapper matchMapper;
    private final ClubService clubService;
    private final EventCoHostOrganizerService eventCoHostOrganizerService;
    private final EventOrganizerService eventOrganizerService;


    /**
     * The function to create email reminder template for the event either by user input or default template.
     *
     * @param emailConfig     The email configuration.
     * @param recipientEmails The players' emails to be reminded.
     * @param emailContent    The content of user input.
     * @return EmailVo
     */
    private static EmailVo getEmailVo(EmailConfig emailConfig, Event event,
                                      List<String> recipientEmails, String emailContent) {
        if (emailConfig.getId() == null) {
            throw new BadRequestException("Please configure email settings first.");
        }

        String subject = "Reminder: Check-in for event " + event.getName();
        String content = "<p>Hi,</p>" +
                "<p>This is a reminder to check in for the event: <strong>" + event.getName() + "</strong>.</p>" +
                "<p>The event is scheduled for: " + event.getEventTime() + ". Please make sure to check in on time.</p>" +
                "<p>Thank you!</p>";

        // Get content from user input; otherwise use default template
        if (emailContent != null) {
            content = emailContent;
        }

        return new EmailVo(recipientEmails, subject, content);
    }

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
        // default sorting by eventTime DESC
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "eventTime")
            );
        }

        Page<Event> page = eventRepository.findAll(buildEventSpecification(criteria), pageable);
        return PageUtil.toPage(page.map(event -> {
            event.setClub(clubService.findEntityById(event.getClub().getId()));
            return eventMapper.toDto(event);
        }));
    }

    private Specification<Event> buildEventSpecification(EventQueryCriteria criteria) {
        return (root, query, builder) -> {
            Predicate predicate = QueryHelp.getPredicate(root, criteria, builder);

            if (criteria.getEventTimeFilter() != null) {
                Timestamp now = new Timestamp(System.currentTimeMillis());
                if (criteria.getEventTimeFilter() == EventTimeFilter.UPCOMING) {
                    predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get("eventTime"), now));
                } else if (criteria.getEventTimeFilter() == EventTimeFilter.PAST) {
                    predicate = builder.and(predicate, builder.lessThan(root.get("eventTime"), now));
                }
            }

            final EventOrganizer eventOrganizer = eventOrganizerService.findCurrentUserEventOrganizer();
            if (eventOrganizer != null) {
                Set<Long> clubIds = eventOrganizer.getClubs().stream()
                        .map(Club::getId)
                        .collect(Collectors.toSet());

                // Subquery for co-hosted events
                Subquery<Long> coHostSubquery = query.subquery(Long.class);
                Root<EventCoHostOrganizer> coHostRoot = coHostSubquery.from(EventCoHostOrganizer.class);
                coHostSubquery.select(coHostRoot.get("event").get("id"))
                        .where(builder.equal(coHostRoot.get("eventOrganizer").get("id"), eventOrganizer.getId()));

                Predicate isMainOrganizer = builder.equal(root.get("organizer").get("id"), eventOrganizer.getId());

                Predicate isNullOrganizerWithMatchingClub = builder.and(
                        builder.isNull(root.get("organizer")),
                        !clubIds.isEmpty() ? root.get("club").get("id").in(clubIds) : builder.disjunction()
                );

                Predicate isCoHost = root.get("id").in(coHostSubquery);
                predicate = builder.and(predicate,
                        builder.or(
                                isMainOrganizer,
                                isNullOrganizerWithMatchingClub,
                                isCoHost
                        )
                );
            }

            return predicate;
        };
    }


    @Transactional
    public EventDto findById(Long id) {
        Event event = eventRepository.findById(id).orElseGet(Event::new);
        ValidationUtil.isNull(event.getId(), "Event", "id", id);
        return eventMapper.toDto(event);
    }

    /**
     * @param resource Event resource to be created
     * @return EventDto
     */
    @Transactional(rollbackFor = Exception.class)
    public EventDto create(EventDto resource) {
        validateEventTime(resource);//validate event time
        validateMaxParticipants(resource);// validate minimum participants

        Long currentUserId = SecurityUtils.getCurrentUserId();
        var event = eventMapper.toEntity(resource);

        // Club must not be empty when creating an event
        final Club club = clubService.findEntityById(resource.getClubId());
        if (club == null) {
            throw new EntityNotFoundException(Club.class, "id", resource.getClubId());
        }

        // Set main organizer
        Optional<EventOrganizer> organizerList = eventOrganizerRepository.findFirstByUserId(currentUserId);
        organizerList.ifPresent(mainOrganizer -> {
            // validate co_host organizer account
            eventCoHostOrganizerService.validateOrganizerAccount(mainOrganizer);

            // main organizer must be a member of the club
            validateOrganizerClubPermission(mainOrganizer, resource.getClubId());
            event.setOrganizer(mainOrganizer);
        });

        // Set the creator of the event using the Long ID directly
        if (resource.getCreateBy() == null) { // Event.java has 'createBy' as Long
            event.setCreateBy(currentUserId);
        }

        // If organizerList is empty, it means the user is not an organizer (e.g., an admin),
        // so the check is bypassed. Permission to create is handled by @PreAuthorize.
        event.setClub(club);
        event.setStatus(EventStatus.PUBLISHED);
        Set<Tag> processedTags = processIncomingTags(resource.getTags());
        event.setTags(processedTags);

        final Event eventResult = eventRepository.save(event);

        // Set co_host organizers or throw exception
        eventCoHostOrganizerService.createEventCoHostOrganizers(resource.getCoHostOrganizers(), eventResult);

        EventDto responseDto = eventMapper.toDto(eventResult);
        String eventLink = EVENT_BASE_URL + eventResult.getId();
        responseDto.setPublicLink(eventLink);
        return responseDto;
    }

    /**
     * The function to validate minimum number of participants in the event
     *
     * @param resource The event resource to be validated
     */
    private void validateMaxParticipants(EventDto resource) {
        final int minParticipants = EVENT_FORMAT_PLAYERS.get(resource.getFormat()) * resource.getGroupCount();
        if (resource.getMaxParticipants() != null && resource.getMaxParticipants() < minParticipants) {
            throw new BadRequestException("Max participants must be at least " + minParticipants);
        }
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
        validateCheckInCheckOut(resources);

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

        if (event.getStatus() != EventStatus.PUBLISHED && event.getStatus() != EventStatus.CHECK_IN) {
            throw new BadRequestException("Event is not open for joining");
        }

        // Prevent duplicate registration for the same event
        if (teamPlayerRepository.findByEventId(event.getId()).stream().anyMatch(tp -> tp.getPlayer().getId().equals(playerId))) {
            throw new BadRequestException("Player is already registered for this event");
        }

        // Allow waitlist if current participants are over max participants & event allows waitlist.
        boolean isWaitList = false;
        final int currentParticipants = event.getCurrentParticipants() == null ? 0 : event.getCurrentParticipants();
        if (currentParticipants < event.getMaxParticipants() && event.isAllowWaitList()) {
            isWaitList = true;
        } else if (currentParticipants >= event.getMaxParticipants() && !event.isAllowWaitList()) {
            throw new BadRequestException("Event is full and does not allow waitlist");
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
    public EventDto withdrawFromEvent(Long eventId, EventActionDTO request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "id", String.valueOf(eventId)));

        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BadRequestException("Withdrawal is not allowed at the current event status: " + event.getStatus());
        }

        // Check if player is on the main list
        TeamPlayer teamPlayer = teamPlayerRepository.findByEventIdAndPlayerId(eventId, request.playerId());
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
            WaitList waitListEntry = waitListRepository.findByEventIdAndPlayerId(eventId, request.playerId());
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
     * Delete an event related match, team, team player and waitlist
     * for any event with status PUBLISHED and CLOSED
     *
     * @param id The event ID to be deleted
     * @return ExecutionResult
     **/
    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult deleteById(Long id) {
        successfulDelete(id);
        Map<String, Object> data = new HashMap<>();
        data.put("success", "Event has been deleted successfully");
        return ExecutionResult.of(id, data);
    }

    /**
     * Delete all event related match, team, team player and waitlist
     * for any event with status PUBLISHED and CLOSED
     *
     * @param ids The event ID to be deleted
     * @return ExecutionResult
     **/
    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult deleteAll(Long[] ids) {
        List<Long> successfulDeletes = new ArrayList<>();
        List<Long> failedDeletes = new ArrayList<>();

        for (Long id : ids) {
            try {
                successfulDelete(id);
                successfulDeletes.add(id);
            } catch (EntityNotFoundException e) {
                failedDeletes.add(id);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("successfulDeletes", successfulDeletes.size());
        data.put("failedDeletes", failedDeletes.size());
        data.put("details", Map.of("successfulIds", successfulDeletes, "failedIds", failedDeletes));

        Long operationId = ids.length > 0 ? ids[0] : null;
        if (!failedDeletes.isEmpty()) {
            operationId = failedDeletes.get(0);
        }
        return ExecutionResult.of(operationId, data);
    }

    /**
     * Delete an event related match, team, team player and waitlist
     * for any event with status PUBLISHED and CLOSED. Then it returns successful status or not
     *
     * @param id The event ID to be deleted
     **/
    private void successfulDelete(final Long id) {
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
            }

            throw new BadRequestException("Cannot delete event with status " + event.getStatus());
        }

        throw new EntityNotFoundException(Event.class, "id", String.valueOf(id));
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
        if (remindDto.getPlayers().isEmpty() && !remindDto.isAllPlayers()) {
            return ExecutionResult.of(id, Map.of("message", "Please select at least one player to be reminded"));
        }

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Event.class, "id", String.valueOf(id)));

        // Get all players to be reminded and have not checked in yet
        List<TeamPlayer> playersToRemind = teamPlayerRepository.
                findByEventIdAndPlayerIdsOrAllPlayers(event.getId(), remindDto.getPlayers(), remindDto.isAllPlayers())
                .stream().toList();

        if (playersToRemind.isEmpty()) {
            return ExecutionResult.of(id, Map.of("message", "No players to remind."));
        }

        // Get all emails of players to be reminded
        List<String> recipientEmails = playersToRemind.stream()
                .map(teamPlayer -> userRepository.findById(teamPlayer.getPlayer().getUser().getId()).orElse(null))
                .filter(Objects::nonNull)
                .map(me.zhengjie.modules.system.domain.User::getEmail)
                .toList();

        if (recipientEmails.isEmpty()) {
            return ExecutionResult.of(id, Map.of("message", "No valid emails found for players to remind."));
        }

        // Set up email dto and send email
        var emailConfig = emailService.find();
        EmailVo emailVo = getEmailVo(emailConfig, event, recipientEmails, remindDto.getContent());
        emailService.send(emailVo, emailConfig);

        return ExecutionResult.of(id, Map.of("remindersSent", recipientEmails.size()));
    }

    /**
     * The function to finalize all groups for the event. No changes allowed after this.
     *
     * @param eventId ID of the event to be finalized the groups
     */
    @Transactional
    public void finalizedGroup(Long eventId) {
        final var matchGroup = matchGroupRepository.findAllByEventId(eventId);
        // Check if all groups have been finalized
        final boolean allGroupFinalized = matchGroup
                .stream().allMatch(MatchGroup::getIsFinalized);

        if (allGroupFinalized) {
            throw new BadRequestException("All groups have already been finalized");
        }

        final var updatedGroup = matchGroup
                .stream()
                .peek(group -> group.setIsFinalized(true)).toList();
        matchGroupRepository.saveAll(updatedGroup);
    }

    /**
     * The function to relocate a team from one group to another
     *
     * @param request The team and group information to be relocated
     */
    @Transactional
    public void relocateTeam(final TeamRelocationDTO request) {
        final Long teamId = request.getTeamId();
        final Long targetGroupId = request.getTargetGroupId();

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException(Team.class, "id", teamId.toString()));
        MatchGroup targetGroup = matchGroupRepository.findById(targetGroupId)
                .orElseThrow(() -> new EntityNotFoundException(MatchGroup.class, "id", targetGroupId.toString()));


        int currentTargetSize = teamRepository.countTeamByGroupId(request.getTargetGroupId());
        if (currentTargetSize >= targetGroup.getGroupTeamSize()) {
            throw new BadRequestException("Target group is full");
        }

        MatchGroup currentGroup = team.getMatchGroup();
        if (currentGroup.getId().equals(request.getTargetGroupId())) {
            throw new BadRequestException("Relocation to the same group is not allowed");
        }


        if (team.getStatus() != TeamStatus.CHECKED_IN) {
            throw new BadRequestException("Only checked-in teams can be moved between groups.");
        }

        team.setMatchGroup(targetGroup);
        teamRepository.save(team);
    }

    @Transactional
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
                    .map(matchMapper::toDto)
                    .toList();
            groupDto.setMatches(matches);
            return groupDto;
        }).toList();
    }

    /**
     * Validation & Set check-in and check-out time: whether event time is in the future
     */
    private void validateEventTime(EventDto event) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (event.getEventTime().before(now)) {
            throw new BadRequestException("Event time cannot be in the past.");
        }

        if (event.getCheckInEnd() == null) {
            event.setCheckInEnd(event.getEventTime()); // Default to event start time
        }

        if (event.getCheckInStart() == null) {
            event.setCheckInStart(new Timestamp(event.getEventTime().getTime() - 60 * 60 * 1000)); // Default to 1 hour before
        }

        if (event.getCheckInStart() != null && event.getCheckInStart().after(event.getEventTime())) {
            throw new BadRequestException("Check-in start time cannot be after event time.");
        }

        validateCheckInCheckOut(event);
    }


    /**
     * Validation: whether check-in is before check-out and check-in is before event time
     */
    private <T extends EventTimeDto> void validateCheckInCheckOut(T event) {
        if (event.getCheckInStart() != null && event.getCheckInEnd() != null && event.getEventTime() != null) {
            if (!(event.getCheckInStart().before(event.getCheckInEnd())
                    && event.getCheckInEnd().before(event.getEventTime())
                    || event.getCheckInEnd().equals(event.getEventTime()))) {
                throw new BadRequestException("Check-in window must be before or at event time and start before end.");
            }
        }
    }
}