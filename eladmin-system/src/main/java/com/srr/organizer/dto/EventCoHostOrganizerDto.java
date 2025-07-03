package com.srr.organizer.dto;

import com.srr.event.domain.Event;
import com.srr.organizer.domain.EventOrganizer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Cheyleap
 * @description Event co host organizer data transfer object
 * @date 2025-07-03
 **/
@Data
public class EventCoHostOrganizerDto {
    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "Event that this co host is associated with")
    private Event event;

    @ApiModelProperty(value = "Event organizer who participates in the event as a  co host")
    private EventOrganizer eventOrganizer;
}