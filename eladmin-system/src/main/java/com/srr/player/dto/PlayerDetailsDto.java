package com.srr.player.dto;

import com.srr.event.dto.EventDto;
import com.srr.event.dto.MatchDto;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.List;

@Data
@Accessors(chain = true)
public class PlayerDetailsDto {
    private PlayerDto player;
    private Integer singleEventRating;
    private Integer DoubleEventRating;
    private Integer singleEventRatingChanges;
    private Integer DoubleEventRatingChanges;
    private Integer totalEvent;
    private MatchDto lastMatch;
    private LinkedHashMap<String, Integer> singleEventRatingHistory;
    private LinkedHashMap<String, Integer> doubleEventRatingHistory;
    private EventDto eventToday;
    private List<EventDto> upcomingEvents;
}
