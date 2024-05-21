package com.example.springbootbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("productclass")
public class ProductClass {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String pids;
    private String name;
    private String description;
    private String regulation;
}
