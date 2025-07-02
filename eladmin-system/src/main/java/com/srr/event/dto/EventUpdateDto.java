package com.srr.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = false)
@Getter
@Setter
public class EventUpdateDto extends EventTimeDto {
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
}
