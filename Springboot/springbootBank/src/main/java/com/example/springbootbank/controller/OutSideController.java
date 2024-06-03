package com.example.springbootbank.controller;

import cn.hutool.core.date.DateUtil;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.News;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

//该controller层主要用于接入外部接口
@RestController
@RequestMapping("/outSide")
public class OutSideController {
    @PostMapping("/bank")//银行卡安全，用于向中国银行请求账户的其他银行行为
    public Result save(@RequestBody Map map){
        //首先向中国银行发送本银行的安全验证
        //银行安全验证成功后向中国银行发生需要查询对象的信息（身份信息）
        //得到中国银行传递来的该用户的其他银行信息
        return Result.success();
    }

    @PostMapping("/shenfen")//向警察系统中的身份系统请求
    public Result shenfen(@RequestBody Map map){
        //首先向身份系统发送本银行的安全验证
        //银行安全验证成功后向身份系统发生需要查询对象的信息（身份信息）
        //得到验证结果
        return Result.success();
    }
}
