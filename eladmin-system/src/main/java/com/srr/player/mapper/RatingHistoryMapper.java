package com.srr.player.mapper;

import com.srr.player.domain.RatingHistory;
import com.srr.player.dto.RatingHistoryDto;
import me.zhengjie.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
* @author Chanheng
* @date 2025-05-26
**/
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RatingHistoryMapper extends BaseMapper<RatingHistoryDto, RatingHistory> {

    /**
     * Entity to DTO mapping with explicit field mappings
     * @param entity RatingHistory entity
     * @return RatingHistoryDto
     */
    @Override
    @Mapping(source = "player.id", target = "playerId")
    @Mapping(source = "player.name", target = "playerName")
    @Mapping(source = "match.id", target = "matchId")
    RatingHistoryDto toDto(RatingHistory entity);
}
