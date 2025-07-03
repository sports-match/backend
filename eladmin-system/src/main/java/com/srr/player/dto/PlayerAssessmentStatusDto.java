package com.srr.player.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for player assessment status check
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerAssessmentStatusDto {
    private boolean assessmentCompleted;
    private String message;
    private Long playerId;
}
