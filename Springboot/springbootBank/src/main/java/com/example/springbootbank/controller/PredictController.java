package com.example.springbootbank.controller;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.MarketName;
import com.example.springbootbank.entity.Product;
import com.example.springbootbank.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/Predict")
public class PredictController {
    @Resource
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CodeMapper codeMapper;

    @Autowired
    private BankCardMapper bankCardMapper;
    @Autowired
    private ProductMapper productMapper;


    @Autowired
    UserProductMapper userProductMapper;

    @Autowired
    CreditCardMapper creditCardMapper;
    @Autowired
    MarketMapper marketMapper;

    @Autowired
    MarketNameMapper marketNameMapper;
    @Autowired
    UserLoansMapper userLoansMapper;

    @Autowired
    UserCreditMapper userCreditMapper;

    @Autowired
    EnterpriseMapper enterpriseMapper;
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    MarketCopyMapper marketCopyMapper;
    @Autowired
    PredictMapper predictMapper;

    @PostMapping("/getProductMarketRate")//获取选中商品对应市场的变化利率来计算利率
    public Result getProduct(@RequestBody JSONArray mids){
        System.out.println(mids);
        List<MarketName> marketNames=new ArrayList<>();
        for(int i=0;i<mids.size();i++){//遍历这些市场
            MarketName marketName=marketNameMapper.selectById((Integer)mids.get(i));
            marketNames.add(marketName);
        }
        if(marketNames.size()>0){
            return Result.success(marketNames);
        }
        return Result.error("300","系统错误！");
    }

}
