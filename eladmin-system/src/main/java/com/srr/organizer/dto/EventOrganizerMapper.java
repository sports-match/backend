package com.srr.organizer.dto;

import com.srr.organizer.domain.EventOrganizer;
import me.zhengjie.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author Chanheng
 * @date 2025-05-26
 **/
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventOrganizerMapper extends BaseMapper<EventOrganizerDto, EventOrganizer> {
    EventOrganizerDto toDto(EventOrganizer entity);
}
