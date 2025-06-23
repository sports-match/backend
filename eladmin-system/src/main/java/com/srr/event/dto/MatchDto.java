package com.srr.event.dto;

import com.srr.player.dto.TeamDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
* @description /
* @author Chanheng
* @date 2025-05-25
**/
@Data
public class MatchDto implements Serializable {

    @ApiModelProperty(value = "id")
    private Long id;
    
    @ApiModelProperty(value = "Match Group id")
    private MatchGroupDto matchGroup;
    
    @ApiModelProperty(value = "Team A id")
    private TeamDto teamA;
    
    @ApiModelProperty(value = "Team B id")
    private TeamDto teamB;
    
    @ApiModelProperty(value = "Score A")
    private Integer scoreA;
    
    @ApiModelProperty(value = "Score B")
    private Integer scoreB;
    
    @ApiModelProperty(value = "Team A Win")
    private Boolean teamAWin;
    
    @ApiModelProperty(value = "Team B Win")
    private Boolean teamBWin;
    
    @ApiModelProperty(value = "Score Verified")
    private Boolean scoreVerified;
}
