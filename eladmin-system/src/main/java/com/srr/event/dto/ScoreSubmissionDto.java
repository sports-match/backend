package com.srr.event.dto;

import lombok.Data;
import java.util.List;

@Data
public class ScoreSubmissionDto {
    private Long eventId;
    private List<Long> matchIds; // Matches to submit (all with scores entered)
}
