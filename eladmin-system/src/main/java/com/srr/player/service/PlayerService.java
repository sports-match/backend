package com.srr.player.service;

import com.srr.enumeration.Format;
import com.srr.event.dto.EventMapper;
import com.srr.event.mapper.MatchMapper;
import com.srr.event.repository.EventRepository;
import com.srr.event.repository.MatchRepository;
import com.srr.player.domain.Player;
import com.srr.player.domain.PlayerSportRating;
import com.srr.player.dto.*;
import com.srr.player.mapper.PlayerMapper;
import com.srr.player.mapper.RatingHistoryMapper;
import com.srr.player.repository.PlayerRepository;
import com.srr.player.repository.PlayerSportRatingRepository;
import com.srr.player.repository.RatingHistoryRepository;
import com.srr.sport.service.SportService;
import lombok.RequiredArgsConstructor;
import me.zhengjie.exception.EntityNotFoundException;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Chanheng
 * @description 服务实现
 * @date 2025-05-18
 **/
@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final PlayerSportRatingRepository playerSportRatingRepository;
    private final RatingHistoryRepository ratingHistoryRepository;
    private final MatchRepository matchRepository;
    private final SportService sportService;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final MatchMapper matchMapper;
    private final RatingHistoryMapper ratingHistoryMapper;


    public PageResult<PlayerDto> queryAll(PlayerQueryCriteria criteria, Pageable pageable) {
        Page<Player> page = playerRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        // Map to DTOs and enrich with ratings
        Page<PlayerDto> dtoPage = page.map(player -> {
            PlayerDto dto = playerMapper.toDto(player);
            dto.setSportRatings(playerSportRatingRepository.findByPlayerId(player.getId())
                    .stream()
                    .map(rating -> {
                        PlayerSportRatingDto dtoRating = new PlayerSportRatingDto();
                        dtoRating.setId(rating.getId());
                        dtoRating.setPlayerId(rating.getPlayerId());
                        dtoRating.setSportId(rating.getSportId());
                        dtoRating.setFormat(rating.getFormat());
                        dtoRating.setRateScore(rating.getRateScore());
                        dtoRating.setRateBand(rating.getRateBand());
                        dtoRating.setProvisional(rating.getProvisional());
                        dtoRating.setCreateTime(rating.getCreateTime());
                        dtoRating.setUpdateTime(rating.getUpdateTime());
                        return dtoRating;
                    })
                    .collect(java.util.stream.Collectors.toList()));
            return dto;
        });
        return PageUtil.toPage(dtoPage);
    }


    public List<PlayerDto> queryAll(PlayerQueryCriteria criteria) {
        return playerMapper.toDto(playerRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
    }


    @Transactional
    public PlayerDto findById(Long id) {
        Player player = playerRepository.findById(id).orElseGet(Player::new);
        ValidationUtil.isNull(player.getId(), "Player", "id", id);
        return playerMapper.toDto(player);
    }

    public PlayerDetailsDto findPlayerDetailsById(Long id) {
        var playerDto = playerRepository.findById(id)
                .map(playerMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException(Player.class, "id", id));

        var eventToday = eventRepository.getPlayerEventToday(id)
                .map(eventMapper::toDto)
                .orElse(null);

        var upcomingEvent = eventRepository.getPlayerUpcomingEvents(id)
                .stream()
                .map(eventMapper::toDto)
                .toList();

        final var badminton = sportService.getBadminton();

        double singleRating = 0;
        double singleRatingChanges = 0;
        double doubleRating = 0;
        double doubleRatingChanges = 0;

        var singleRatingsHistory = ratingHistoryRepository.findByPlayerIdOrderByCreateTimeDesc(id);

        if (singleRatingsHistory != null) {
            singleRating = singleRatingsHistory.get(0).getRateScore();
            singleRatingChanges = singleRatingsHistory.get(0).getChanges();
        }

        var doubleRatingsHistory = ratingHistoryRepository.findByPlayerIdOrderByCreateTimeDesc(id);
        if (doubleRatingsHistory != null) {
            doubleRating = doubleRatingsHistory.get(0).getRateScore();
            doubleRatingChanges = doubleRatingsHistory.get(0).getChanges();
        }

        var playerEvents = eventRepository.getPlayerCompletedEvents(id);
        var allEventsJoined = playerEvents == null ? 0 : playerEvents.size();

        var lastMatch  = matchRepository.getByPlayerId(id);

        return new PlayerDetailsDto()
                .setPlayer(playerDto)
                .setSingleEventRating(singleRating)
                .setSingleEventRatingChanges(singleRatingChanges)
                .setDoubleEventRating(doubleRating)
                .setDoubleEventRatingChanges(doubleRatingChanges)
                .setTotalEvent(allEventsJoined)
                .setEventToday(eventToday)
                .setLastMatch(matchMapper.toDto(lastMatch))
                .setUpcomingEvents(upcomingEvent)
                .setSingleEventRatingHistory(ratingHistoryMapper.toDto(singleRatingsHistory))
                .setDoubleEventRatingHistory(ratingHistoryMapper.toDto(doubleRatingsHistory));
    }


    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult create(Player resources) {
        Player savedPlayer = playerRepository.save(resources);
        // No default answers; users will submit their own self-assessment
        return ExecutionResult.of(savedPlayer.getId());
    }


    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult update(Player resources) {
        Player player = playerRepository.findById(resources.getId()).orElseGet(Player::new);
        ValidationUtil.isNull(player.getId(), "Player", "id", resources.getId());
        player.copy(resources);
        Player savedPlayer = playerRepository.save(player);
        return ExecutionResult.of(savedPlayer.getId());
    }


    @Transactional
    public ExecutionResult deleteAll(Long[] ids) {
        for (Long id : ids) {
            playerRepository.deleteById(id);
        }
        return ExecutionResult.of(null, Map.of("count", ids.length, "ids", ids));
    }


    public Player findByUserId(Long userId) {
        return playerRepository.findByUserId(userId);
    }


    public PlayerAssessmentStatusDto checkAssessmentStatus(Long sportId) {
        // Get current user ID
        Long currentUserId = SecurityUtils.getCurrentUserId();
        // Find the player associated with the current user
        Player player = findByUserId(currentUserId);
        if (player == null) {
            return new PlayerAssessmentStatusDto(false, "Player profile not found. Please create your profile first.");
        }
        // Check if the player has completed the self-assessment using PlayerSportRating (Badminton/DOUBLES as example)
        boolean isAssessmentCompleted = false;
        Optional<PlayerSportRating> ratingOpt = playerSportRatingRepository.findByPlayerIdAndSportIdAndFormat(player.getId(), sportId, Format.DOUBLE);
        if (ratingOpt.isPresent() && ratingOpt.get().getRateScore() != null && ratingOpt.get().getRateScore() > 0) {
            isAssessmentCompleted = true;
        }
        String message = isAssessmentCompleted
                ? "Self-assessment completed."
                : "Please complete your self-assessment before joining any events.";
        return new PlayerAssessmentStatusDto(isAssessmentCompleted, message);
    }
}