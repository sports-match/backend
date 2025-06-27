package com.srr.player.service;

import com.srr.enumeration.Format;
import com.srr.event.dto.EventMapper;
import com.srr.event.repository.EventRepository;
import com.srr.player.domain.Player;
import com.srr.player.domain.PlayerSportRating;
import com.srr.player.dto.*;
import com.srr.player.mapper.PlayerMapper;
import com.srr.player.repository.PlayerRepository;
import com.srr.player.repository.PlayerSportRatingRepository;
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
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;


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
                        dtoRating.setSport(rating.getSport());
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

        var eventToday = eventRepository.getPlayerEvents(id)
                .map(eventMapper::toDto)
                .orElse(null);

        var upcomingEventToday = eventRepository.getPlayerUpcomingEvents(id)
                .stream()
                .map(eventMapper::toDto)
                .toList();

        return new PlayerDetailsDto()
                .setPlayer(playerDto)
                .setDoubleEventRating(1000)
                .setSingleEventRating(1000)
                .setDoubleEventRatingChanges(100)
                .setSingleEventRatingChanges(-100)
                .setTotalEvent(10)
                .setEventToday(eventToday)
                .setLastMatch(null)
                .setUpcomingEvents(upcomingEventToday)
                .setSingleEventRatingHistory(null)
                .setDoubleEventRatingHistory(null);
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


    public PlayerAssessmentStatusDto checkAssessmentStatus() {
        // Get current user ID
        Long currentUserId = SecurityUtils.getCurrentUserId();
        // Find the player associated with the current user
        Player player = findByUserId(currentUserId);
        if (player == null) {
            return new PlayerAssessmentStatusDto(false, "Player profile not found. Please create your profile first.");
        }
        // Check if the player has completed the self-assessment using PlayerSportRating (Badminton/DOUBLES as example)
        boolean isAssessmentCompleted = false;
        Optional<PlayerSportRating> ratingOpt = playerSportRatingRepository.findByPlayerIdAndSportAndFormat(player.getId(), "Badminton", Format.DOUBLE);
        if (ratingOpt.isPresent() && ratingOpt.get().getRateScore() != null && ratingOpt.get().getRateScore() > 0) {
            isAssessmentCompleted = true;
        }
        String message = isAssessmentCompleted
                ? "Self-assessment completed."
                : "Please complete your self-assessment before joining any events.";
        return new PlayerAssessmentStatusDto(isAssessmentCompleted, message);
    }
}