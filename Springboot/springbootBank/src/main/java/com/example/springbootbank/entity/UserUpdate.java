package com.example.springbootbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("userupdate")
public class UserUpdate {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer uid;
    private Integer cid;
    private Integer type;
    private Integer state;
    private String passmsg;
    private float cost;
    private Integer rank;
}
