package com.srr.player.mapper;

import com.srr.player.domain.PlayerSportRating;
import com.srr.player.dto.PlayerSportRatingDto;
import me.zhengjie.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlayerSportRatingMapper extends BaseMapper<PlayerSportRatingDto, PlayerSportRating> {
}
