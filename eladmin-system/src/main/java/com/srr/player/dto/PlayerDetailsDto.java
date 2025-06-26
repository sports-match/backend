package com.srr.player.dto;

import com.srr.event.dto.EventDto;
import com.srr.event.dto.MatchDto;

import java.util.LinkedHashMap;
import java.util.LinkedList;

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
    private LinkedList<EventDto> upcomingEvents;
}
