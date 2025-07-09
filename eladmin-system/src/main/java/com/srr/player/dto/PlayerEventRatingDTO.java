package com.srr.player.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PlayerEventRatingDTO {
    private String name;
    private Integer wins;
    private Integer losses;
    private Double previousRating;
    private Double newRating;
    private Double ratingChanges;
}
