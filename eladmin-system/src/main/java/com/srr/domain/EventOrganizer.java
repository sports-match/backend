package com.srr.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.srr.enumeration.VerificationStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Chanheng
 * @description Event organizer entity
 * @date 2025-05-26
 **/
@Entity
@Data
@Table(name = "event_organizer")
public class EventOrganizer implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    @ApiModelProperty(value = "id", hidden = true)
    private Long id;

    @Column(name = "`description`")
    @ApiModelProperty(value = "Description")
    private String description;

    @Column(name = "`create_time`")
    @CreationTimestamp
    @ApiModelProperty(value = "Creation time", hidden = true)
    private Timestamp createTime;

    @Column(name = "`update_time`")
    @UpdateTimestamp
    @ApiModelProperty(value = "Update time", hidden = true)
    private Timestamp updateTime;

    @Column(name = "`user_id`", nullable = false)
    @NotNull
    @ApiModelProperty(value = "userId")
    private Long userId;

    @ManyToMany
    @JoinTable(name = "organizer_club",
            joinColumns = @JoinColumn(name = "organizer_id"),
            inverseJoinColumns = @JoinColumn(name = "club_id"))
    @ApiModelProperty(value = "Clubs this organizer can manage")
    private Set<Club> clubs = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "`verification_status`")
    @ApiModelProperty(value = "Verification Status")
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    public void copy(EventOrganizer source) {
        BeanUtil.copyProperties(source, this, CopyOptions.create().setIgnoreNullValue(true));
    }

    public void addClub(Club club) {
        if (clubs != null) {
            this.clubs.add(club);
        } else {
            this.clubs = new HashSet<>();
            this.clubs.add(club);
        }

    }
}
