package com.example.springbootbank.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.Enterprise;
import com.example.springbootbank.entity.User;
import com.example.springbootbank.mapper.BankCardMapper;
import com.example.springbootbank.mapper.EnterpriseMapper;
import com.example.springbootbank.mapper.MarketNameMapper;
import com.example.springbootbank.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/enterprise")
public class EnterpriseController {
    @Resource

    @Autowired
    BankCardMapper bankCardMapper;

    @Autowired
    MarketNameMapper marketNameMapper;

    @Autowired
    EnterpriseMapper enterpriseMapper;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/IsEnterprise")//查看是不是企业，并返回
    public Result getMarketName(@RequestBody Map map){
        Integer uid=(Integer) map.get("uid");
        Enterprise enterprise=enterpriseMapper.selectOne(Wrappers.<Enterprise>lambdaQuery().eq(Enterprise::getUid,uid));
        if(enterprise!=null){
            return Result.success(enterprise);
        }
        return Result.error("500","普通用户");
    }

}
