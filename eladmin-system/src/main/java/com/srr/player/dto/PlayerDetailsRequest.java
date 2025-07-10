package com.srr.player.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PlayerDetailsRequest {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
