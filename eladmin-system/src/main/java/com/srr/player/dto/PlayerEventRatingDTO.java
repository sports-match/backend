package com.srr.player.dto;

import com.srr.utils.NumberConverter;
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

    // Custom getters
    public Long getPreviousRating() {
        return NumberConverter.doubleToLong(previousRating);
    }

    public Long getNewRating() {
        return NumberConverter.doubleToLong(newRating);
    }

    public Long getRatingChanges() {
        return NumberConverter.doubleToLong(ratingChanges);
    }
}
