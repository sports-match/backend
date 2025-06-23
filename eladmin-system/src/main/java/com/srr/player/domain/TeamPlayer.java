package com.srr.player.domain;

import com.srr.enumeration.TeamPlayerStatus;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Getter
@Setter
@Entity
@Table(name = "team_player")
public class TeamPlayer implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ApiModelProperty(value = "id", hidden = true)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    @Column(name = "is_checked_in")
    private boolean isCheckedIn;

    @Column(name = "registration_time")
    private Timestamp registrationTime;

    @Column(name = "check_in_time")
    private Timestamp checkInTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TeamPlayerStatus status = TeamPlayerStatus.REGISTERED;

    @PrePersist
    @PreUpdate
    public void updateTeamAverageScore() {
        // No-op: Average score calculation should be handled in a service, not in the entity.
    }
}
