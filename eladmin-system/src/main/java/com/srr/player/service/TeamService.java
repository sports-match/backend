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
package com.srr.player.service;

import com.srr.player.domain.Team;
import com.srr.player.dto.TeamDto;
import com.srr.player.mapper.TeamMapper;
import com.srr.player.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import me.zhengjie.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Chanheng
 * @date 2025-05-30
 **/
@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;

    
    @Transactional(readOnly = true)
    public TeamDto findById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Team.class, "id", id.toString()));
        return teamMapper.toDto(team);
    }

    
    @Transactional(readOnly = true)
    public List<TeamDto> findByEventId(Long eventId) {
        List<Team> teams = teamRepository.findByEventId(eventId);
        return teams.stream()
                .map(teamMapper::toDto)
                .collect(Collectors.toList());
    }
}
