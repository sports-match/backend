package com.srr.player.dto;

import com.srr.event.dto.EventDto;
import com.srr.event.dto.MatchDto;
import com.srr.utils.NumberConverter;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class PlayerDetailsDto {
    private PlayerDto player;
    private Double singleEventRating;
    private Double doubleEventRating;
    private Double singleEventRatingChanges;
    private Double doubleEventRatingChanges;
    private Integer totalEvent;
    private MatchDto lastMatch;
    private List<RatingHistoryDto> singleEventRatingHistory;
    private List<RatingHistoryDto> doubleEventRatingHistory;
    private EventDto eventToday;
    private List<EventDto> upcomingEvents;

    // Custom getters
    public Long getSingleEventRating() {
        return NumberConverter.doubleToLong(singleEventRating);
    }

    public Long getDoubleEventRating() {
        return NumberConverter.doubleToLong(doubleEventRating);
    }

    public Long getSingleEventRatingChanges() {
        return NumberConverter.doubleToLong(singleEventRatingChanges);
    }

    public Long getDoubleEventRatingChanges() {
        return NumberConverter.doubleToLong(doubleEventRatingChanges);
    }
}
