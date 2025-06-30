package com.srr.player.dto;

import com.srr.enumeration.Format;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * Request DTO for submitting player self-assessment answers with sport/format.
 */
@Data
public class PlayerSelfAssessmentRequest implements Serializable {

    private List<PlayerAnswerDto> answers;

    @NotNull
    private Long sportId; // optional, default to "Badminton"
    private Format format = Format.DOUBLE; // optional, default to "DOUBLES"
}
