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
package com.srr.club.service;

import com.srr.club.domain.Club;
import com.srr.club.dto.ClubDto;
import com.srr.club.dto.ClubMapper;
import com.srr.club.dto.ClubQueryCriteria;
import com.srr.club.repository.ClubRepository;
import com.srr.organizer.domain.EventOrganizer;
import com.srr.organizer.service.EventOrganizerService;
import lombok.RequiredArgsConstructor;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Chanheng
 * @website https://eladmin.vip
 * @description 服务实现
 * @date 2025-05-18
 **/
@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubMapper clubMapper;
    private final EventOrganizerService eventOrganizerService;


    public PageResult<ClubDto> queryAll(ClubQueryCriteria criteria, Pageable pageable) {
        Page<Club> page = clubRepository.findAll(buildEventSpecification(criteria), pageable);
        return PageUtil.toPage(page.map(clubMapper::toDto));
    }

    private Specification<Club> buildEventSpecification(ClubQueryCriteria criteria) {
        return (root, query, builder) -> {
            Predicate predicate = QueryHelp.getPredicate(root, criteria, builder);
            final EventOrganizer eventOrganizer = eventOrganizerService.findCurrentUserEventOrganizer();

            if (eventOrganizer != null) {
                Set<Club> organizerClubs = eventOrganizer.getClubs();
                if (organizerClubs != null && !organizerClubs.isEmpty()) {
                    List<Long> clubIds = organizerClubs.stream()
                            .map(Club::getId)
                            .collect(Collectors.toList());

                    predicate = builder.and(predicate, root.get("id").in(clubIds));
                }
            }

            return predicate;
        };
    }


    public List<ClubDto> queryAll(ClubQueryCriteria criteria) {
        return clubMapper.toDto(clubRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
    }


    @Transactional
    public ClubDto findById(Long id) {
        Club club = clubRepository.findById(id).orElseGet(Club::new);
        ValidationUtil.isNull(club.getId(), "Club", "id", id);
        return clubMapper.toDto(club);
    }

    @Transactional
    public Club findEntityById(Long id) {
        Club club = clubRepository.findById(id).orElseGet(Club::new);
        ValidationUtil.isNull(club.getId(), "Club", "id", id);
        return club;
    }


    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult create(Club resources) {
        Club savedClub = clubRepository.save(resources);
        return ExecutionResult.of(savedClub.getId());
    }


    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult update(Club resources) {
        Club club = clubRepository.findById(resources.getId()).orElseGet(Club::new);
        ValidationUtil.isNull(club.getId(), "Club", "id", resources.getId());
        club.copy(resources);
        Club savedClub = clubRepository.save(club);
        return ExecutionResult.of(savedClub.getId());
    }


    @Transactional
    public ExecutionResult deleteAll(Long[] ids) {
        for (Long id : ids) {
            clubRepository.deleteById(id);
        }
        return ExecutionResult.of(null, Map.of("count", ids.length, "ids", ids));
    }


    public void download(List<ClubDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ClubDto club : all) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("名称", club.getName());
            map.put("描述", club.getDescription());
            map.put("创建时间", club.getCreateTime());
            map.put("更新时间", club.getUpdateTime());
            map.put("图标", club.getIcon());
            map.put("排序", club.getSort());
            map.put("是否启用", club.getEnabled());
            map.put("位置", club.getLocation());
            map.put("经度", club.getLongitude());
            map.put("纬度", club.getLatitude());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}