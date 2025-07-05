package com.srr.player.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import me.zhengjie.annotation.Query;

/**
 * @author Chanheng
 * @date 2025-05-18
 **/
@Data
public class PlayerQueryCriteria {

    /**
     * 精确
     */
    @Query
    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * 精确
     */
    @Query
    @ApiModelProperty(value = "userId")
    private Long userId;

    @Query(type = Query.Type.INNER_LIKE)
    @ApiModelProperty(value = "name")
    private String name;
}