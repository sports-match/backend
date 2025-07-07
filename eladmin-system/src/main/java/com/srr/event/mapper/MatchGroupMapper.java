package com.srr.event.mapper;

import com.srr.event.domain.MatchGroup;
import com.srr.event.dto.MatchGroupDto;
import com.srr.player.mapper.TeamMapper;
import me.zhengjie.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * @author Chanheng
 * @date 2025-05-25
 **/
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {TeamMapper.class, MatchMapper.class})
public interface MatchGroupMapper extends BaseMapper<MatchGroupDto, MatchGroup> {
    @Mapping(target = "matchCount", expression = "java(calculateMatchCount(matchGroup))")
    MatchGroupDto toDto(MatchGroup matchGroup);

    /**
     * Calculate the number of matches in a round-robin tournament for the given match group
     *
     * @param matchGroup the match group to calculate matches for
     * @return number of matches, or 0 if teams are null or empty
     */
    default Integer calculateMatchCount(MatchGroup matchGroup) {
        if (matchGroup == null || matchGroup.getTeams() == null || matchGroup.getTeams().size() < 2) {
            return 0;
        }
        int teamCount = matchGroup.getTeams().size();
        return (teamCount * (teamCount - 1)) / 2;
    }
}
