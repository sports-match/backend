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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Chanheng
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
}