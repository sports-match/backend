package com.srr.event.mapper;

import com.srr.event.domain.MatchGroup;
import com.srr.event.dto.MatchGroupDto;
import com.srr.player.mapper.TeamMapper;
import me.zhengjie.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
* @author Chanheng
* @date 2025-05-25
**/
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = TeamMapper.class)
public interface MatchGroupMapper extends BaseMapper<MatchGroupDto, MatchGroup> {

}
