package com.example.springbootbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.models.auth.In;
import lombok.Data;

@Data
@TableName("admin_online")
public class AdminOnline {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer u_id;
    private String name;
    private Integer online;
    private Integer role;
}
