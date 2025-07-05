package com.srr.club.dto;

import com.srr.club.domain.Court;
import me.zhengjie.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
* @author Chanheng
* @date 2025-05-18
**/
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourtMapper extends BaseMapper<CourtDto, Court> {

}