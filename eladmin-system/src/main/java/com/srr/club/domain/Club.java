package com.srr.club.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.sql.Timestamp;

/**
* @description /
* @author Chanheng
* @date 2025-05-18
**/
@Entity
@Data
@Table(name="club")
public class Club implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ApiModelProperty(value = "id", hidden = true)
    private Long id;

    @Column(name = "name",nullable = false)
    @NotBlank
    @ApiModelProperty(value = "Name")
    private String name;

    @Column(name = "description")
    @ApiModelProperty(value = "Description")
    private String description;

    @Column(name = "create_time")
    @CreationTimestamp
    @ApiModelProperty(value = "Creation time", hidden = true)
    private Timestamp createTime;

    @Column(name = "update_time")
    @UpdateTimestamp
    @ApiModelProperty(value = "Update time", hidden = true)
    private Timestamp updateTime;

    @Column(name = "icon")
    @ApiModelProperty(value = "Icon")
    private String icon;

    @Column(name = "sort")
    @ApiModelProperty(value = "Sort")
    private Integer sort;

    @Column(name = "enabled")
    @ApiModelProperty(value = "Enabled")
    private Boolean enabled;

    @Column(name = "location")
    @ApiModelProperty(value = "Location")
    private String location;

    @Column(name = "longitude")
    @ApiModelProperty(value = "Longitude")
    private Double longitude;

    @Column(name = "latitude")
    @ApiModelProperty(value = "Latitude")
    private Double latitude;

    public void copy(Club source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
