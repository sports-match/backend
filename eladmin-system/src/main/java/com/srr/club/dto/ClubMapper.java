package com.srr.club.dto;

import com.srr.club.domain.Club;
import me.zhengjie.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
* @author Chanheng
* @date 2025-05-18
**/
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClubMapper extends BaseMapper<ClubDto, Club> {

}