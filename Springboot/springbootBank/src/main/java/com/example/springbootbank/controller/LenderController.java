package com.example.springbootbank.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.Lender;
import com.example.springbootbank.entity.Market;
import com.example.springbootbank.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/Lender")
public class LenderController {
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
    LenderMapper lenderMapper;
    @PostMapping("/getLenders")//获取全部贷款类型
    public Result getLenders(@RequestBody Map map){
        List<Lender> lenders=lenderMapper.selectList(null);
        if(lenders.size()>0){
            return Result.success(lenders);
        }
        return Result.error("500","暂无贷款产品信息");
    }
}
