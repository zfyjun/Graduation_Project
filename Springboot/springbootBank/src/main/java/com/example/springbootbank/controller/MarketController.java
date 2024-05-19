package com.example.springbootbank.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.*;
import com.example.springbootbank.mapper.BankCardMapper;
import com.example.springbootbank.mapper.MarketCopyMapper;
import com.example.springbootbank.mapper.MarketMapper;
import com.example.springbootbank.mapper.MarketNameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
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

    @Autowired
    MarketCopyMapper marketCopyMapper;
    @PostMapping("/getMarketTimebyId")//根据id获取市场最早时间与最晚时间
    public Result getMarketTimebyId(@RequestBody Map map){
        Integer mid=(Integer) map.get("mid");
        QueryWrapper<Market> queryWrapper=new QueryWrapper<Market>();
        List<Market>list=marketMapper.selectList(queryWrapper.eq("marketid",mid).orderByAsc("date"));
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("btime",list.get(0).getDate());
        jsonObject.put("etime",list.get(list.size()-1).getDate());
        if(list.size()>45){
            jsonObject.put("stime",list.get(list.size()-45).getDate());
        }
        else {
            jsonObject.put("stime",list.get(0).getDate());
        }
        if(list.size()>0){
            return Result.success(jsonObject);
        }
        return Result.error("500","暂无该时间段内市场的市场数据");
    }
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
        LocalDateTime now=LocalDateTime.now();
        Integer sum=howmenytimes();
        for(int i=0;i<markets.size();i++){
            Market market= marketMapper.selectOne(Wrappers.<Market>lambdaQuery().eq(Market::getMarketid,markets.get(i).getMarketid()).eq(Market::getDate,markets.get(i).getDate()));
            if(market!=null){//有时间上的重复的
                Integer id=market.getId();
                markets.get(i).setId(id);//设置对应的id
                cmarketList.add(markets.get(i));
            }
            else {
                if(marketMapper.insert(markets.get(i))==1){//记录插入数据
                    if(!setcopy(2,markets.get(i),now,sum)){
                        return Result.error("500","数据备份时出现错误！");
                    }
                }
                else {
                    return Result.error("500","存在数据导入失败，进程中止");
                }
            }
        }
        if(cmarketList.size()>0){
            return Result.success(cmarketList);
        }
        return Result.success();
    }

    @PostMapping("/recost")//撤销操作
    public Result recost(@RequestBody List<MarketCopy> marketCopyList){
        //排序
        List<List<MarketCopy>> listList=new ArrayList<>();//记录所有
        for(int i=marketCopyList.size()-1;i>=0;i--){//倒叙访问
            MarketCopy marketCopy=marketCopyMapper.selectById(marketCopyList.get(i).getId());//获取当前需要撤回的操作数据
            Integer yuanid=marketCopy.getQuondamid();
            List<MarketCopy> insertmarkets=new ArrayList<>();//记录被更新的插入数据的列表(每个)
            //验证通过
            if(marketCopy.getChangeway()==2){//插入撤回，操作
                //删除插入操作前先进行时间验证，看有没有在这条操作后面又对该数据进行了操作
                QueryWrapper<MarketCopy> queryWrapper=new QueryWrapper<>();
                List<MarketCopy> marketCopies=marketCopyMapper.selectList(queryWrapper.eq("isdelete",0).eq("quondamid",yuanid));
                int flag=0;
                for(int j=marketCopies.size()-1;j>=0;j--){
                    if(marketCopies.get(j).getChangedate().isAfter(marketCopy.getChangedate())&&marketCopies.get(j).getId()!=marketCopy.getId()){
                       flag=1;
                       insertmarkets.add(marketCopies.get(j));
                    }
                }
                if(flag==0){
                    if(marketMapper.deleteById(marketCopy.getQuondamid())==1){//删除插入的数据
                        marketCopy.setIsdelete(1);
                        if(marketCopyMapper.updateById(marketCopy)==1){//删除操作记录
                        }
                        else {
                            return Result.error("500","操作记录删除失败");
                        }
                    }
                    else {
                        return Result.error("500","数据删除失败");
                    }
                }
                else {
                    insertmarkets.add(0,marketCopy);
                    listList.add(insertmarkets);
                }
            }
            else if(marketCopy.getChangeway()==1){//覆盖操作，还原
                Market market=marketMapper.selectById(marketCopy.getQuondamid());
                market.setLow(marketCopy.getLow());
                market.setHigh(marketCopy.getHigh());
                market.setClose(marketCopy.getClose());
                market.setOpen(marketCopy.getOpen());
                market.setDate(marketCopy.getDate());
                market.setAdjustedclose(marketCopy.getAdjustedclose());
                market.setVolume(marketCopy.getVolume());
                if(marketMapper.updateById(market)==1){//还原数据
                    marketCopy.setIsdelete(1);
                    if(marketCopyMapper.updateById(marketCopy)==1){//删除记录(删除位标记为1)
                    }
                    else {
                        return Result.error("500","删除记录失败");
                    }
                }
                else {
                    return Result.error("500","数据还原失败");
                }
            }
        }
        return Result.success(listList);
    }
    @PostMapping("/updatedate")//更新时间重复的数据为最新数据
    public Result updatedate(@RequestBody List<Market> markets){
        LocalDateTime now=LocalDateTime.now();
        Integer sum=howmenytimes();
        for(int i=0;i<markets.size();i++){
            Market market=marketMapper.selectById(markets.get(i).getId());//原数据
            if(marketMapper.updateById(markets.get(i))==1){//覆盖成功，更新对应的数据备份
                if(setcopy(1,market,now,sum)){//传入原数据
                }
                else{
                    return Result.error("500","数据备份时出现错误！");
                }
            }
            else {//覆盖失败
                return Result.error("500","错误！存在数据更新失败");
            }
        }
        System.out.println("结束");
        return Result.success();
    }
    @PostMapping("/getcopys")//获取备份信息(分页查询)
    public Result getcopys(@RequestBody Map map){
        Integer pageNum=(Integer) map.get("pageNum");
        Integer pageSize=(Integer) map.get("pageSize");
        Integer types=(Integer) map.get("types");
        Integer mid=(Integer) map.get("mid");
        Page<MarketCopy> page= Page.of(pageNum,pageSize);
        QueryWrapper<MarketCopy> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("mid",mid).eq("isdelete",0);
        if(types!=0){
            queryWrapper.eq("changeway",types);
        }
        marketCopyMapper.selectPage(page,queryWrapper);
        return Result.success(page);
    }

    @PostMapping("/updatepredict")//上传最新预测数据更新数据库
    public Result updatepredict(@RequestBody Map map){
        Integer mid=(Integer) map.get("mid");
        float predict=Float.valueOf((Integer) map.get("predict")) ;
        MarketName marketName=marketNameMapper.selectById(mid);
        //获取当前市场的最新数据
        QueryWrapper<Market> marketQueryWrapper=new QueryWrapper<>();
        marketQueryWrapper.orderByDesc("date");
        List<Market> marketList=marketMapper.selectList(marketQueryWrapper);
        marketName.setNowdata(marketList.get(0).getAdjustedclose());
        marketName.setPredictdata(predict);
        marketName.setPredicttime(LocalDateTime.now());
        float rates=(marketName.getPredictdata()/marketName.getNowdata());
        if(rates>=1.1&&rates<1.3){//上升
            marketName.setEvaluation(1);
        }
        else if(rates>=1.3){//大幅度上升
            marketName.setEvaluation(2);
        }
        else if(rates<1.1&&rates>0.9){//保持
            marketName.setEvaluation(3);
        }
        else if(rates<=0.9&&rates>0.7){//下降
            marketName.setEvaluation(4);
        }
        else if(rates<=0.7){//大幅下降
            marketName.setEvaluation(5);
        }
        if(marketNameMapper.updateById(marketName)==1){
            return Result.success(marketName);
        }
        return Result.error("500","更新失败！");
    }
    public boolean setcopy(Integer type, Market market, LocalDateTime now,Integer sum){//数据备份信息
        MarketCopy marketCopy=new MarketCopy();
        //记录数据
        marketCopy.setMid(market.getMarketid());
        marketCopy.setAdjustedclose(market.getAdjustedclose());
        marketCopy.setHigh(market.getHigh());
        marketCopy.setDate(market.getDate());
        marketCopy.setLow(market.getLow());
        marketCopy.setOpen(market.getOpen());
        marketCopy.setClose(market.getClose());
        marketCopy.setVolume(market.getVolume());
        //记录成功，设置状态信息
        marketCopy.setChangeway(type);//设置类型
        marketCopy.setManytimes(sum);//设置次数
        marketCopy.setQuondamid(market.getId());// 原数据位置
        marketCopy.setChangedate(now);//设置更改时间
        if(marketCopyMapper.insert(marketCopy)==1){
            return true;
        }
      return false;
    }

    public Integer howmenytimes(){//获取最新次数，得到本次次数
       Integer sum=1;
       QueryWrapper<MarketCopy> queryWrapper=new QueryWrapper<>();
       queryWrapper.orderByDesc("manytimes");
       List<MarketCopy> marketCopies=marketCopyMapper.selectList(queryWrapper);
       if(marketCopies.size()>0){
           sum=marketCopies.get(0).getManytimes()+1;
       }
       return sum;
    }
}
//线性回归类
