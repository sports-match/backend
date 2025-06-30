package com.srr.player.mapper;

import com.srr.player.domain.TeamPlayer;
import com.srr.player.dto.TeamPlayerDto;
import me.zhengjie.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author Chanheng
 * @date 2025-05-25
 **/
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = PlayerMapper.class)
public interface TeamPlayerMapper extends BaseMapper<TeamPlayerDto, TeamPlayer> {

}
