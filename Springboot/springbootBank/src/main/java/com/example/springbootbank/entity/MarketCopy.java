package com.example.springbootbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("marketcopy")
public class MarketCopy {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    private Integer quondamid;
    private Integer changeway;
    private LocalDateTime changedate;
    private Integer manytimes;
    private Integer date;
    private float open;
    private float high;
    private float low;
    private float close;
    private float volume;
    private float adjustedclose;
}
