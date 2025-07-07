package com.srr.event.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class TeamRelocationDTO {
    @NotNull(message = "Team ID must not be null")
    private Long teamId;

    @NotNull(message = "The target group ID must not be null")
    private Long targetGroupId;
}
