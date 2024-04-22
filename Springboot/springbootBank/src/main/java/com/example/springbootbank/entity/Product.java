package com.example.springbootbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product")//产品
public class Product {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private Integer type;
    private double rate;
    private Integer minday;
    private LocalDateTime createtime;
    private Integer risk;
    private String description;
    private Integer amount;
    private Integer price;
    private Integer sum;
    private String targetmarket;
    private String historicalrate;
}
