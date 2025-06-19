package com.srr.player.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Request DTO for submitting player self-assessment answers with sport/format.
 */
@Data
public class PlayerSelfAssessmentRequest implements Serializable {
    private List<PlayerAnswerDto> answers;
    private String sport; // optional, default to "Badminton"
    private String format; // optional, default to "DOUBLES"
}
