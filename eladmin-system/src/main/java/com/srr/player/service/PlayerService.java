package com.srr.player.service;

import com.srr.enumeration.Format;
import com.srr.player.domain.Player;
import com.srr.player.domain.PlayerSportRating;
import com.srr.player.dto.PlayerAssessmentStatusDto;
import com.srr.player.dto.PlayerDto;
import com.srr.player.dto.PlayerQueryCriteria;
import com.srr.player.dto.PlayerSportRatingDto;
import com.srr.player.mapper.PlayerMapper;
import com.srr.player.repository.PlayerRepository;
import com.srr.player.repository.PlayerSportRatingRepository;
import com.srr.sport.service.SportService;
import lombok.RequiredArgsConstructor;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

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
    private final SportService sportService;


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


    public void download(List<PlayerDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (PlayerDto player : all) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("名称", player.getName());
            map.put("描述", player.getDescription());
            map.put("纬度", player.getLatitude());
            map.put("经度", player.getLongitude());
            map.put("图片", player.getProfileImage());
            map.put("创建时间", player.getCreateTime());
            map.put("更新时间", player.getUpdateTime());
            map.put("评分", ""); // Legacy field removed, optionally fetch from PlayerSportRating if needed
            map.put(" userId", player.getUserId());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
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
        var badminton = sportService.getBadminton();
        Optional<PlayerSportRating> ratingOpt = playerSportRatingRepository.findByPlayerIdAndSportIdAndFormat(player.getId(), badminton.getId(), Format.DOUBLE);
        if (ratingOpt.isPresent() && ratingOpt.get().getRateScore() != null && ratingOpt.get().getRateScore() > 0) {
            isAssessmentCompleted = true;
        }
        String message = isAssessmentCompleted
                ? "Self-assessment completed."
                : "Please complete your self-assessment before joining any events.";
        return new PlayerAssessmentStatusDto(isAssessmentCompleted, message);
    }
}