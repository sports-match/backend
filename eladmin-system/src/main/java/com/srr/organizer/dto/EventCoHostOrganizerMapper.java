package com.srr.organizer.dto;

import com.srr.organizer.domain.EventCoHostOrganizer;
import com.srr.organizer.domain.EventOrganizer;
import me.zhengjie.base.BaseMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import javax.swing.text.html.parser.Entity;

/**
 * @author Cheyleap
 * @date 2025-07-03
 **/
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventCoHostOrganizerMapper extends BaseMapper<EventOrganizerDto, EventOrganizer> {
    EventCoHostOrganizer toEntity(EventCoHostOrganizerDto resource);

    EventCoHostOrganizerDto toDto(Entity resource);
}