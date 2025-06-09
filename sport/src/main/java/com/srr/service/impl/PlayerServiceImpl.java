/*
*  Copyright 2019-2025 Zheng Jie
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package com.srr.service.impl;

import com.srr.domain.Player;
import com.srr.dto.PlayerAssessmentStatusDto;
import me.zhengjie.utils.ValidationUtil;
import me.zhengjie.utils.FileUtil;
import lombok.RequiredArgsConstructor;
import com.srr.repository.PlayerRepository;
import com.srr.service.PlayerService;
import com.srr.dto.PlayerDto;
import com.srr.dto.PlayerQueryCriteria;
import com.srr.dto.mapstruct.PlayerMapper;
import me.zhengjie.utils.ExecutionResult;
import me.zhengjie.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import me.zhengjie.utils.PageUtil;
import me.zhengjie.utils.QueryHelp;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import me.zhengjie.utils.PageResult;
import com.srr.repository.QuestionRepository;
import com.srr.repository.PlayerAnswerRepository;
import com.srr.domain.Question;
import com.srr.domain.PlayerAnswer;
import com.srr.repository.PlayerSportRatingRepository;
import com.srr.domain.PlayerSportRating;
import java.util.Optional;

/**
* @description 服务实现
* @author Chanheng
* @date 2025-05-18
**/
@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final QuestionRepository questionRepository;
    private final PlayerAnswerRepository playerAnswerRepository;
    private final PlayerSportRatingRepository playerSportRatingRepository;

    @Override
    public PageResult<PlayerDto> queryAll(PlayerQueryCriteria criteria, Pageable pageable){
        Page<Player> page = playerRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        // Map to DTOs and enrich with ratings
        Page<PlayerDto> dtoPage = page.map(player -> {
            PlayerDto dto = playerMapper.toDto(player);
            dto.setSportRatings(playerSportRatingRepository.findByPlayerId(player.getId())
                .stream()
                .map(rating -> {
                    com.srr.dto.PlayerSportRatingDto dtoRating = new com.srr.dto.PlayerSportRatingDto();
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

    @Override
    public List<PlayerDto> queryAll(PlayerQueryCriteria criteria){
        return playerMapper.toDto(playerRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder)));
    }

    @Override
    @Transactional
    public PlayerDto findById(Long id) {
        Player player = playerRepository.findById(id).orElseGet(Player::new);
        ValidationUtil.isNull(player.getId(),"Player","id",id);
        return playerMapper.toDto(player);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult create(Player resources) {
        Player savedPlayer = playerRepository.save(resources);
        // No default answers; users will submit their own self-assessment
        return ExecutionResult.of(savedPlayer.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult update(Player resources) {
        Player player = playerRepository.findById(resources.getId()).orElseGet(Player::new);
        ValidationUtil.isNull( player.getId(),"Player","id",resources.getId());
        player.copy(resources);
        Player savedPlayer = playerRepository.save(player);
        return ExecutionResult.of(savedPlayer.getId());
    }

    @Override
    @Transactional
    public ExecutionResult deleteAll(Long[] ids) {
        for (Long id : ids) {
            playerRepository.deleteById(id);
        }
        return ExecutionResult.of(null, Map.of("count", ids.length, "ids", ids));
    }

    @Override
    public void download(List<PlayerDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (PlayerDto player : all) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("名称", player.getName());
            map.put("描述", player.getDescription());
            map.put("纬度", player.getLatitude());
            map.put("经度", player.getLongitude());
            map.put("图片", player.getProfileImage());
            map.put("创建时间", player.getCreateTime());
            map.put("更新时间", player.getUpdateTime());
            map.put("评分", ""); // Legacy field removed, optionally fetch from PlayerSportRating if needed
            map.put(" userId",  player.getUserId());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Player findByUserId(Long userId) {
        return playerRepository.findByUserId(userId);
    }

    @Override
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
        Optional<PlayerSportRating> ratingOpt = playerSportRatingRepository.findByPlayerIdAndSportAndFormat(player.getId(), "Badminton", "DOUBLES");
        if (ratingOpt.isPresent() && ratingOpt.get().getRateScore() != null && ratingOpt.get().getRateScore() > 0) {
            isAssessmentCompleted = true;
        }
        String message = isAssessmentCompleted 
            ? "Self-assessment completed." 
            : "Please complete your self-assessment before joining any events.";
        return new PlayerAssessmentStatusDto(isAssessmentCompleted, message);
    }
}