
package com.example.springbootbank.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.MarketName;
import com.example.springbootbank.entity.Product;
import com.example.springbootbank.mapper.BankCardMapper;
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
@RequestMapping("/MarketName")
public class MarketNameController {
    @Resource

    @Autowired
    BankCardMapper bankCardMapper;

    @Autowired
    MarketNameMapper marketNameMapper;

    @PostMapping("/ProductToGetMarketName")//获取当前商品的市场对象
    public Result getMarketName(@RequestBody Map map){
        String targetmarket=(String) map.get("targetmarket");
        JSONArray jsonArray=JSONArray.parseArray(targetmarket);
        if(jsonArray.size()>0){
            List<MarketName> marketNames=new ArrayList<>();
            for(int i=0;i<jsonArray.size();i++){
                JSONObject json=jsonArray.getJSONObject(i);
                MarketName marketName=marketNameMapper.selectById(json.getInteger("id"));
                marketNames.add(marketName);
            }
            return Result.success(marketNames);
        }
        return Result.error("500","该产品暂无明确目标市场");
    }

    @PostMapping("/getMarketNames")//获取所有市场名称数据
    public Result getMarketNames(@RequestBody Map map){
        List<MarketName>marketNames=marketNameMapper.selectList(null);
        if(marketNames.size()>0){
            return Result.success(marketNames);
        }
        return Result.error("500","获取数据为空！");
    }
}
