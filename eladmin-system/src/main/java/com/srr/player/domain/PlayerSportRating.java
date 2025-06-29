package com.srr.player.domain;

import com.srr.enumeration.Format;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "player_sport_rating")
@Getter
@Setter
public class PlayerSportRating implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long playerId;

    @Column(name = "sport_id")
    private Long sportId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Format format; // e.g., "singles", "doubles"

    @Column
    private Double rateScore;

    @Column
    private String rateBand;

    @Column
    private Boolean provisional = true;

    @CreationTimestamp
    private Timestamp createTime;

    @UpdateTimestamp
    private Timestamp updateTime;
}
