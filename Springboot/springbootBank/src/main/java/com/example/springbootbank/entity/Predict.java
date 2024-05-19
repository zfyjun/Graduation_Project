package com.example.springbootbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("predict")//预测数据
public class Predict {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    private Integer marketid;
    private Integer date;
    private float open;
    private float high;
    private float low;
    private float close;
    private float volume;
    private float adjustedclose;
}
