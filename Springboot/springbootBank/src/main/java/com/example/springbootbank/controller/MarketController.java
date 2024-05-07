package com.example.springbootbank.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.Market;
import com.example.springbootbank.entity.MarketName;
import com.example.springbootbank.entity.User;
import com.example.springbootbank.mapper.BankCardMapper;
import com.example.springbootbank.mapper.MarketMapper;
import com.example.springbootbank.mapper.MarketNameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

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
        return Result.error("500","暂无该时间段内市场的市场数据");
    }
    @PostMapping("/adddate")//上传市场数据到数据库
    public Result adddate(@RequestBody List<Market> markets){
        List<Market> cmarketList=new ArrayList<>();
        for(int i=0;i<markets.size();i++){
            Market market= marketMapper.selectOne(Wrappers.<Market>lambdaQuery().eq(Market::getMarketid,markets.get(i).getMarketid()).eq(Market::getDate,markets.get(i).getDate()));
            if(market!=null){//有时间上的重复的
                Integer id=market.getId();
                System.out.println(id);
                markets.get(i).setId(id);//设置对应的id
                cmarketList.add(markets.get(i));
            }
            else {
                if(marketMapper.insert(markets.get(i))==1){
                }
                else {
                    return Result.error("500","存在数据导入失败，进程中止");
                }
            }
        }
        if(cmarketList.size()>0){
            return Result.success(cmarketList);
        }
        return Result.error("500","暂无该时间段内市场的市场数据");
    }

    @PostMapping("/updatedate")//更新时间重复的数据为最新数据
    public Result updatedate(@RequestBody List<Market> markets){
        Integer flag=0;
        Market market=new Market();
        for(int i=0;i<markets.size();i++){
            if(marketMapper.updateById(markets.get(i))!=1){
                flag=1;
                return Result.error("500","错误！存在数据更新失败");
            }
        }
        if(flag==0){//无意外产生
            return Result.success();
        }
        return Result.error("500","系统错误");
    }
}
//线性回归类
