package com.srr.player.dto;

import com.srr.event.dto.EventDto;
import com.srr.event.dto.MatchDto;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class PlayerDetailsDto {
    private PlayerDto player;
    private Double singleEventRating;
    private Double DoubleEventRating;
    private Double singleEventRatingChanges;
    private Double DoubleEventRatingChanges;
    private Integer totalEvent;
    private MatchDto lastMatch;
    private List<RatingHistoryDto> singleEventRatingHistory;
    private List<RatingHistoryDto> doubleEventRatingHistory;
    private EventDto eventToday;
    private List<EventDto> upcomingEvents;
}
