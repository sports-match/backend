package com.srr.event.dto;

import javax.validation.constraints.NotNull;

public record EventActionDTO(@NotNull(message = "Player ID is required") Long playerId) {
}
