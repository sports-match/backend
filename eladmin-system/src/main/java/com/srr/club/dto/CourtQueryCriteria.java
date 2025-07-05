package com.srr.club.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import me.zhengjie.annotation.Query;

/**
* @author Chanheng
* @date 2025-05-18
**/
@Data
public class CourtQueryCriteria{

    /** 精确 */
    @Query
    @ApiModelProperty(value = "id")
    private Long id;

    /** 精确 */
    @Query
    @ApiModelProperty(value = "clubId")
    private Long clubId;

    /** 精确 */
    @Query
    @ApiModelProperty(value = "sportId")
    private Long sportId;
}