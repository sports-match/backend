package com.srr.player.domain;

import com.srr.event.domain.Match;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "rating_history")
public class RatingHistory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ApiModelProperty(value = "id", hidden = true)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne
    @JoinColumn(name = "player_sport_rating_id")
    private PlayerSportRating playerSportRating;

    @Column(name = "rate_score")
    @ApiModelProperty(value = "Score")
    private Double rateScore;

    @Column(name = "changes")
    @ApiModelProperty(value = "Changes")
    private Double changes;

    @Column(name = "create_time")
    @CreationTimestamp
    @ApiModelProperty(value = "Creation time", hidden = true)
    private LocalDateTime createTime;

    @ManyToOne
    @JoinColumn(name = "match_id")
    private Match match;
}
