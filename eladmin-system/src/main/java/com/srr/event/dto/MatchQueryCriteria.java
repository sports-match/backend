package com.srr.event.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import me.zhengjie.annotation.Query;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author Chanheng
 * @date 2025-05-26
 */
@Data
public class MatchQueryCriteria {

    @Query
    private Long id;
    
    @Query
    @ApiModelProperty(value = "Event ID")
    private Long eventId;
    
    @Query
    @ApiModelProperty(value = "Match Group ID")
    private Long matchGroupId;
    
    @Query
    @ApiModelProperty(value = "Team A ID")
    private Long teamAId;
    
    @Query
    @ApiModelProperty(value = "Team B ID")
    private Long teamBId;
    
    @Query
    @ApiModelProperty(value = "Court Number")
    private Integer courtNumber;
    
    @Query
    @ApiModelProperty(value = "Match Status")
    private String status;
    
    @Query(type = Query.Type.BETWEEN)
    @ApiModelProperty(value = "Match Time Range")
    private List<Timestamp> matchTime;
    
    @Query(type = Query.Type.BETWEEN)
    @ApiModelProperty(value = "Create time range")
    private List<Timestamp> createTime;
}
