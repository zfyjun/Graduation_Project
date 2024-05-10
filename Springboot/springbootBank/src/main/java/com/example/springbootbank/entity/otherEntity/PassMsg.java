package com.example.springbootbank.entity.otherEntity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PassMsg {
    public LocalDateTime senttime;//提交时间
    public LocalDateTime respondtime;//回应时间
    public String description;//审核内容描述
    public Integer state;//审核状态，0审核中，1审核未通过，2审核通过
}
