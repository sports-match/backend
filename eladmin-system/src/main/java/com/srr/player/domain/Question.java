package com.srr.player.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.srr.event.domain.MatchFormat;
import com.srr.sport.domain.Sport;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.sql.Timestamp;

/**
* @description Question entity for player self-assessment
* @author Chanheng
* @date 2025-05-31
**/
@Entity
@Getter
@Setter
@Table(name="question")
public class Question implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ApiModelProperty(value = "id", hidden = true)
    private Long id;

    @Column(name = "text", nullable = false)
    @NotBlank
    @ApiModelProperty(value = "Question text")
    private String text;

    @Column(name = "category", nullable = false)
    @NotBlank
    @ApiModelProperty(value = "Question category")
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_id", referencedColumnName = "id")
    @ApiModelProperty(value = "Sport")
    private Sport sport;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false)
    @ApiModelProperty(value = "Format (singles/doubles)")
    private MatchFormat format;

    @Column(name = "order_index")
    @ApiModelProperty(value = "Display order")
    private Integer orderIndex;

    @Column(name = "min_value")
    @ApiModelProperty(value = "Minimum value")
    private Integer minValue;

    @Column(name = "max_value")
    @ApiModelProperty(value = "Maximum value")
    private Integer maxValue;

    @Column(name = "create_time")
    @CreationTimestamp
    @ApiModelProperty(value = "Creation time", hidden = true)
    private Timestamp createTime;

    @Column(name = "update_time")
    @UpdateTimestamp
    @ApiModelProperty(value = "Update time", hidden = true)
    private Timestamp updateTime;

    public void copy(Question source){
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
