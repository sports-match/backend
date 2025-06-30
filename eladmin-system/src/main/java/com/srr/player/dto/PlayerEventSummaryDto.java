package com.srr.player.dto;

import com.srr.event.dto.EventDto;
import com.srr.event.dto.MatchDto;
import java.util.List;

public class PlayerEventSummaryDto {
    private EventDto event;
    private List<MatchDto> matches;
    private double netRatingChange;

    public EventDto getEvent() { return event; }
    public PlayerEventSummaryDto setEvent(EventDto event) { this.event = event; return this; }

    public List<MatchDto> getMatches() { return matches; }
    public PlayerEventSummaryDto setMatches(List<MatchDto> matches) { this.matches = matches; return this; }

    public double getNetRatingChange() { return netRatingChange; }
    public PlayerEventSummaryDto setNetRatingChange(double netRatingChange) { this.netRatingChange = netRatingChange; return this; }
}
