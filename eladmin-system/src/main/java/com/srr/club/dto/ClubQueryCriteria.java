package com.srr.club.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import me.zhengjie.annotation.Query;

/**
* @author Chanheng
* @date 2025-05-18
**/
@Data
public class ClubQueryCriteria{

    /** 精确 */
    @Query
    @ApiModelProperty(value = "id")
    private Long id;

    /** 模糊 */
    @Query(type = Query.Type.INNER_LIKE)
    @ApiModelProperty(value = "名称")
    private String name;

    /** 精确 */
    @Query
    @ApiModelProperty(value = "是否启用")
    private Boolean enabled;
}