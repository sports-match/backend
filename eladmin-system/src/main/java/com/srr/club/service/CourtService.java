
package com.srr.club.service;

import com.srr.club.domain.Court;
import com.srr.club.dto.CourtDto;
import com.srr.club.dto.CourtMapper;
import com.srr.club.dto.CourtQueryCriteria;
import com.srr.club.repository.CourtRepository;
import lombok.RequiredArgsConstructor;
import me.zhengjie.utils.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
* @description 服务实现
* @author Chanheng
* @date 2025-05-18
**/
@Service
@RequiredArgsConstructor
public class CourtService {

    private final CourtRepository courtRepository;
    private final CourtMapper courtMapper;

    public PageResult<CourtDto> queryAll(CourtQueryCriteria criteria, Pageable pageable){
        Page<Court> page = courtRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(courtMapper::toDto));
    }

    public List<CourtDto> queryAll(CourtQueryCriteria criteria){
        return courtMapper.toDto(courtRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder)));
    }

    @Transactional
    public CourtDto findById(Long id) {
        Court court = courtRepository.findById(id).orElseGet(Court::new);
        ValidationUtil.isNull(court.getId(),"Court","id",id);
        return courtMapper.toDto(court);
    }

    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult create(Court resources) {
        Court savedCourt = courtRepository.save(resources);
        return ExecutionResult.of(savedCourt.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public ExecutionResult update(Court resources) {
        Court court = courtRepository.findById(resources.getId()).orElseGet(Court::new);
        ValidationUtil.isNull( court.getId(),"Court","id",resources.getId());
        court.copy(resources);
        Court savedCourt = courtRepository.save(court);
        return ExecutionResult.of(savedCourt.getId());
    }

    @Transactional
    public ExecutionResult deleteAll(Long[] ids) {
        for (Long id : ids) {
            courtRepository.deleteById(id);
        }
        return ExecutionResult.of(null, Map.of("count", ids.length, "ids", ids));
    }
}