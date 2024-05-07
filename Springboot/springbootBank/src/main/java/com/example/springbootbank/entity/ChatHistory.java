package com.example.springbootbank.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_history")
public class ChatHistory {

    @TableId("id")
    private String id;

    private String fromUser;
    private String toUser;
    private String text;

    private LocalDateTime createTime;
}
