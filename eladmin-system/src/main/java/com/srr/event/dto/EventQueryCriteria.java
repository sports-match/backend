package com.srr.event.dto;

import com.srr.enumeration.EventStatus;
import com.srr.enumeration.EventTimeFilter;
import com.srr.enumeration.Format;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import me.zhengjie.annotation.Query;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * @author Chanheng
 * @date 2025-05-18
 **/
@Data
public class EventQueryCriteria {

    @Query()
    @ApiModelProperty(value = "status")
    private EventStatus status;

    @Query(propName = "location", joinName = "club", type = Query.Type.INNER_LIKE)
    @ApiModelProperty(value = "location")
    private String location;

    /**
     * 精确
     */
    @Query
    @ApiModelProperty(value = "id")
    private Long id;

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
    @ApiModelProperty(value = "SINGLE, DOUBLE")
    private Format format;

    /**
     * 精确
     */
    @Query
    @ApiModelProperty(value = "clubId")
    private Long clubId;

    @Query
    @ApiModelProperty(value = "sportId")
    private Long sportId;

    /**
     * 精确
     */
    @Query
    @ApiModelProperty(value = "createBy")
    private Long createBy;

    @Query(type = Query.Type.EQUAL_DATE)
    @ApiModelProperty(value = "End date (format: yyyy-MM-dd)")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate eventTime;

    private EventTimeFilter eventTimeFilter;
}