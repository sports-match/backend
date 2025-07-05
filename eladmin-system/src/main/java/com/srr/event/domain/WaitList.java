package com.srr.event.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.srr.enumeration.WaitListStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Event Wait List Entity
 * @author Chanheng
 * @date 2025-05-26
 */
@Entity
@Getter
@Setter
@Accessors(chain = true)
@Table(name = "wait_list")
public class WaitList implements Serializable {

    @Id
    @Column(name = "id")
    @ApiModelProperty(value = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    @NotNull
    @ApiModelProperty(value = "Event ID")
    private Long eventId;

    @Column(name = "player_id", nullable = false)
    @NotNull
    @ApiModelProperty(value = "Player ID")
    private Long playerId;
    
    @Column(name = "notes")
    @ApiModelProperty(value = "Notes")
    private String notes;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @ApiModelProperty(value = "Status (WAITING, PROMOTED, CANCELLED, EXPIRED)")
    private WaitListStatus status = WaitListStatus.WAITING;
    
    @Column(name = "create_time")
    @CreationTimestamp
    @ApiModelProperty(value = "Creation Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp createTime;
    
    @Column(name = "update_time")
    @UpdateTimestamp
    @ApiModelProperty(value = "Update Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Timestamp updateTime;
    
    public void copy(WaitList source) {
        this.notes = source.getNotes();
        this.status = source.getStatus();
    }
}
