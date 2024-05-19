

package com.example.springbootbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_info")
public class UserInfo {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer uid;
    private Integer age;
    private String job;
    private String marital;
    private String education;
    @TableField(exist = false)
    private String credit;
    private Integer balance;
    private String housing;
    private String loan;
    private String contact;
    private Integer day;
    private Integer duration;
    private Integer campaign;
    private Integer pdays;
    private Integer previous;
    private String poutcome;
    private Integer forecast;
}

