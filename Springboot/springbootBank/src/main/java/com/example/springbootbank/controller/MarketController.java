package com.example.springbootbank.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.Market;
import com.example.springbootbank.entity.MarketName;
import com.example.springbootbank.mapper.BankCardMapper;
import com.example.springbootbank.mapper.MarketMapper;
import com.example.springbootbank.mapper.MarketNameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/market")
public class MarketController {
    @Resource

    @Autowired
    BankCardMapper bankCardMapper;

    @Autowired
    MarketNameMapper marketNameMapper;
    @Autowired
    MarketMapper marketMapper;

    @PostMapping("/getMarketDatebyId")//根据id获取市场数据
    public Result getMarketDatebyId(@RequestBody Map map){
        Integer mid=(Integer) map.get("mid");
        Integer startTime=(Integer) map.get("startTime");
        Integer endtime=(Integer) map.get("endtiem") ;
        QueryWrapper<Market> queryWrapper=new QueryWrapper<Market>();
        List<Market>list=marketMapper.selectList(queryWrapper.eq("marketid",mid).between("date",startTime,endtime));
        if(list.size()>0){
            return Result.success(list);
        }
        return Result.error("500","暂无该市场的市场数据");
    }
}
