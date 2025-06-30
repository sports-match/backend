package com.srr.player.dto;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
* @description PlayerAnswer DTO for data transfer
* @author Chanheng
* @date 2025-05-31
**/
@Data
public class PlayerAnswerDto implements Serializable {

    private Long id;
    
    private Long playerId;

    private Long sportId;
    
    private Long questionId;
    
    private Integer answerValue;
    
    private Timestamp createTime;
    
    private Timestamp updateTime;
    
    // Additional fields for data display
    private String questionText;
    
    private String questionCategory;
}
