package com.srr.event.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
public class RemindDto {
    @ApiModelProperty(value = "A set of player ids to be reminded")
    private Set<Long> players;

    @ApiModelProperty(value = "Flag to define if all players should be reminded", required = true)
    @NotNull(message = "All players is required")
    private boolean allPlayers;


    @ApiModelProperty(value = "Message to be sent")
    private String content;
}
