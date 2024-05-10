package com.example.springbootbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("userloans")
public class UserLoans {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer uid;
    private Integer lid;
    private float cost;
    private Integer timelimit;
    private Integer returntype;
    private float salary;
    private String materials;//保存用户材料的id
    private Integer ispass;
    private String passmsg;
}
