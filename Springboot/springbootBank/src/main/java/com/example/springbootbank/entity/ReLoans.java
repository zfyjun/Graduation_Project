package com.example.springbootbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("reloans")//归还贷款
public class ReLoans {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer lid;
    private Integer uid;
    private Integer cid;
    private float cost;
    private LocalDate time;
    private Integer state;
    private LocalDateTime returntime;
    private Integer returnday;
}
