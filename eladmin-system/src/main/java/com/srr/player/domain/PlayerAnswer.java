package com.srr.player.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

/**
* @description Player's answers to self-assessment questions
* @author Chanheng
* @date 2025-05-31
**/
@Entity
@Getter
@Setter
@Table(name="player_answer")
public class PlayerAnswer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ApiModelProperty(value = "id", hidden = true)
    private Long id;

    @Column(name = "player_id", nullable = false)
    @NotNull
    @ApiModelProperty(value = "Player ID")
    private Long playerId;

    @Column(name = "question_id", nullable = false)
    @NotNull
    @ApiModelProperty(value = "Question ID")
    private Long questionId;

    @Column(name = "answer_value", nullable = false)
    @NotNull
    @ApiModelProperty(value = "Answer value")
    private Integer answerValue;

    @Column(name = "create_time")
    @CreationTimestamp
    @ApiModelProperty(value = "Creation time", hidden = true)
    private Timestamp createTime;

    @Column(name = "update_time")
    @UpdateTimestamp
    @ApiModelProperty(value = "Update time", hidden = true)
    private Timestamp updateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", insertable = false, updatable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", insertable = false, updatable = false)
    private Question question;

    public void copy(PlayerAnswer source){
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
