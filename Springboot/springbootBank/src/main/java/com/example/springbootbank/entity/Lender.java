package com.example.springbootbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("lender")
public class Lender {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    private float rate;
    private String name;
    private String description;
}
