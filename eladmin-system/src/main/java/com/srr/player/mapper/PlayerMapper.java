package com.srr.player.mapper;

import com.srr.player.domain.Player;
import com.srr.player.dto.PlayerDto;
import me.zhengjie.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
* @author Chanheng
* @date 2025-05-18
**/
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlayerMapper extends BaseMapper<PlayerDto, Player> {

}