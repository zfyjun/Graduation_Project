package com.example.springbootbank.entity.otherEntity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StateDetail {
    @TableId
    private Integer id;
    private LocalDateTime time;//封禁时间
    private String description;//描述
    private Integer type;//封禁类型（1是挂失，2是长时间不使用，3是违约严重）
}
