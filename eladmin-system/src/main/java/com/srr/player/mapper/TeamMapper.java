package com.srr.player.mapper;

import com.srr.player.domain.Team;
import com.srr.player.dto.TeamDto;
import me.zhengjie.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
* @author Chanheng
* @date 2025-05-25
**/
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = TeamPlayerMapper.class)
public interface TeamMapper extends BaseMapper<TeamDto, Team> {

}
