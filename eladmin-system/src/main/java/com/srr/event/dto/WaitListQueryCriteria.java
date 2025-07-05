package com.srr.event.dto;

import com.srr.enumeration.WaitListStatus;
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
public class WaitListQueryCriteria {

    @Query
    private Long id;
    
    @Query
    @ApiModelProperty(value = "Event ID")
    private Long eventId;
    
    @Query
    @ApiModelProperty(value = "Player ID")
    private Long playerId;
    
    @Query
    @ApiModelProperty(value = "Status")
    private WaitListStatus status;
    
    @Query(type = Query.Type.BETWEEN)
    @ApiModelProperty(value = "Create time range")
    private List<Timestamp> createTime;
}
