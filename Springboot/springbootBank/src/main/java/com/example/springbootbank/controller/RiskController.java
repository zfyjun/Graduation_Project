package com.example.springbootbank.controller;

import cn.hutool.system.UserInfo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.UserLoans;
import com.example.springbootbank.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/Risk")
public class RiskController {
    @Resource

    @Autowired
    BankCardMapper bankCardMapper;

    @Autowired
    ProductMapper productMapper;
    @Autowired
    UserProductMapper userproductMapper;

    @Autowired
    EnterpriseMapper enterpriseMapper;

    @Autowired
    UserProductMapper userProductMapper;

    @Autowired
    UserLoansMapper userLoansMapper;

    @Autowired
    LenderMapper lenderMapper;


    @PostMapping("/riskanalysis")//风险分析
    public Result getLoans(@RequestBody Map map){
        Integer type=(Integer) map.get("type"); //分析类型，1为全部用户进行风险分析，2为只对使用系统的用户分析，3为指定某个用户进行分析
        Integer id=(Integer)map.get("id");//用户id

        return Result.success();
    }

    public boolean userriskanalysis(Integer id){//对单个用户进行分析

        return false;
    }
}
