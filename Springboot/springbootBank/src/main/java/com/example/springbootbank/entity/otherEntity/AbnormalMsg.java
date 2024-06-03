package com.example.springbootbank.entity.otherEntity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AbnormalMsg {
    private LocalDateTime time;
    private String description;
    private Integer state;
    private Integer type;
}
