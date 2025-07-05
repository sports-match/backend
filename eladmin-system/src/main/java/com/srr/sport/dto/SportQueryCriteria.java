package com.srr.sport.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import me.zhengjie.annotation.Query;

import java.sql.Timestamp;

/**
 * @author Chanheng
 * @date 2025-05-17
 **/
@Data
public class SportQueryCriteria {

    /**
     * 模糊
     */
    @Query(type = Query.Type.INNER_LIKE)
    @ApiModelProperty(value = "名称")
    private String name;

    /**
     * 精确
     */
    @Query
    @ApiModelProperty(value = "创建时间")
    private Timestamp createTime;

    /**
     * 精确
     */
    @Query
    @ApiModelProperty(value = "是否启用")
    private Boolean enabled = true;
}