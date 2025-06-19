package com.srr.organizer.dto;

import com.srr.enumeration.VerificationStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OrganizerVerificationDto {

    @NotNull(message = "Verification status cannot be null")
    private VerificationStatus status;
}
