package com.srr.organizer.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
public class EventOrganizerClubDto {
    @NotNull(message = "Clubs cannot be empty")
    @Size(min = 1, message = "At least one club must be selected")
    private Set<Long> clubs;
}
