package com.srr.event.dto;

import com.srr.event.dto.MatchDto;
import com.srr.player.dto.TeamDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
* @description /
* @author Chanheng
* @date 2025-05-25
**/
@Data
public class MatchGroupDto implements Serializable {

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "Name")
    private String name;

    @ApiModelProperty(value = "Event id")
    private Long eventId;
    
    @ApiModelProperty(value = "Group team size")
    private Integer groupTeamSize;

    @ApiModelProperty(value = "Court numbers")
    private String courtNumbers;

    private List<TeamDto> teams;

    // For event results API: matches in this group
    private List<MatchDto> matches;
}
