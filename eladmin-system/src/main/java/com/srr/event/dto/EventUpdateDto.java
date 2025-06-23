package com.srr.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

@JsonIgnoreProperties(ignoreUnknown = false)
@Getter
@Setter
public class EventUpdateDto {
    @ApiModelProperty(value = "Event ID", required = true)
    @NotNull(message = "id is required")
    private Long id;

    @ApiModelProperty(value = "Number of groups")
    private Integer groupCount;

    @ApiModelProperty(value = "Is event public? (true=public, false=private)")
    private Boolean isPublic;

    @ApiModelProperty(value = "Maximum number of participants")
    private Integer maxParticipants;

    @ApiModelProperty(value = "Allow waitlist?")
    private Boolean allowWaitList;

    @ApiModelProperty(value = "Check-in start time (required if updating check-in window)")
    private Timestamp checkInStart;

    @ApiModelProperty(value = "Check-in end time (optional, defaults to event start time if checkInStart is updated)")
    private Timestamp checkInEnd;
}
