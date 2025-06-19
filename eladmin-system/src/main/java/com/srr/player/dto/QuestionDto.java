package com.srr.player.dto;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
* @description Question DTO for data transfer
* @author Chanheng
* @date 2025-05-31
**/
@Data
public class QuestionDto implements Serializable {

    private Long id;
    
    private String text;
    
    private String category;
    
    private Integer orderIndex;
    
    private Integer minValue;
    
    private Integer maxValue;
    
    private Timestamp createTime;
    
    private Timestamp updateTime;
}
