package com.srr.event.domain;

import com.srr.player.domain.Team;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "match_group")
public class MatchGroup implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ApiModelProperty(value = "id", hidden = true)
    private Long id;

    @Column(name = "name")
    @ApiModelProperty(value = "Name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(name = "is_finalized")
    @ApiModelProperty(value = "Finalized status")
    private Boolean isFinalized = false;

    // how many teams in one group
    @Column(name = "group_team_size")
    private int groupTeamSize;

    @Column(name = "court_numbers")
    private String courtNumbers; // e.g., "1,2,3"

    @OneToMany(mappedBy = "matchGroup")
    private List<Team> teams = new ArrayList<>();

    @OneToMany(mappedBy = "matchGroup")
    private List<Match> matches = new ArrayList<>();
}
