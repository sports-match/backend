package com.srr.event.dto;

import javax.validation.constraints.NotNull;

public record EventCheckInDTO(@NotNull(message = "Player ID is required") Long playerId) {
}
