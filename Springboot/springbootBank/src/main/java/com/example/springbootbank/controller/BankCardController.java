

package com.example.springbootbank.controller;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.springbootbank.common.CreateBankCards;
import com.example.springbootbank.common.IdGeneratorSnowlake;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.*;
import com.example.springbootbank.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/BankCard")
public class BankCardController {
    @Resource

    @Autowired
    BankCardMapper bankCardMapper;
    @Autowired
    UserMapper userMapper;

    @Autowired
    CreditCardMapper creditCardMapper;

    @Autowired
    UserLoansMapper userLoansMapper;

    @Autowired
    UserCreditMapper userCreditMapper;

    @Autowired
    ReLoansMapper reLoansMapper;

    @Autowired
    LenderMapper lenderMapper;

    //创建银行卡
    @PostMapping("/creatBankcards")
    public Result creatBankcards(@RequestBody Map map){
        System.out.println(map);
        Integer type=(Integer) map.get("type");
        Integer uid=(Integer) map.get("uid");
        String cardId="";
        while (true){
            CreateBankCards createBankCards=new CreateBankCards();
            cardId=createBankCards.creatBankcards(type);
            BankCard bankCard=bankCardMapper.selectOne(Wrappers.<BankCard>lambdaQuery().eq(BankCard::getCardnumber,cardId));
            if(bankCard==null){
                break;
            }
        }
        BankCard bankCard=new BankCard();
        bankCard.setUid(uid);
        bankCard.setType(type);
        bankCard.setCardnumber(cardId);
        bankCard.setDetail("[]");
        bankCardMapper.insert(bankCard);
        User user=userMapper.selectById(uid);
        //信用卡办理
        if(type==3){
            CreditCard card=new CreditCard();
            card.setCid(bankCard.getId());
            card.setStages("[]");
            card.setDebt("[]");
            card.setLimits(1000);
            card.setOverdue((float) 0.05);
            card.setRate((float) 0.0005);
            creditCardMapper.insert(card);
            bankCard.setBalance(card.getLimits());
            bankCardMapper.updateById(bankCard);
        }

        //更新用户信息
        JSONArray jsonArray =new JSONArray();
        jsonArray=JSONArray.parseArray(user.getBankcards());
        Map maps=new HashMap<>();
        maps.put("id",bankCard.getId());
        jsonArray.add(maps);
        user.setBankcards(jsonArray.toJSONString());
        userMapper.updateById(user);
        //
        return Result.success(cardId);
    }

    @PostMapping("/getCardsDetailbyids")//使用i账单id来获取账单信息
    public Result getCardsDetailbyids(@RequestBody Map map){
        Integer cid=(Integer) map.get("cid");
        List<String> ids=(List<String>) map.get("ids");
        System.out.println(ids);
        List<Detail>lists=new ArrayList<>();
        BankCard bankCard=bankCardMapper.selectById(cid);
        if(bankCard!=null){
            List<Detail> details=JSONArray.parseArray(bankCard.getDetail(),Detail.class);
            System.out.println(details);
            for(int i=0;i<ids.size();i++){
                for(int j=0;j<details.size();j++){
                    if(ids.get(i).equals(String.valueOf(details.get(j).getId()))){
                        lists.add(details.get(j));
                        break;
                    }
                }
            }
            if(lists.size()>0){
                return Result.success(lists);
            }
            return Result.error("400","暂无消费记录");
        }
        return Result.error("404","不存在银行卡");
    }
    @PostMapping("/getCards")//获取用户所有银行卡
    public Result getCards(@RequestBody Map map){
        Integer uid=(Integer) map.get("uid");
        User user=userMapper.selectById(uid);
        JSONArray jsonArray=JSONArray.parseArray(user.getBankcards());
        List <BankCard>cards=new ArrayList<BankCard>();
        if(jsonArray!=null){
            QueryWrapper<BankCard> queryWrapper=new QueryWrapper<>();
            queryWrapper.select("id","type","balance","cardnumber");
            cards=bankCardMapper.selectList(queryWrapper.eq("uid",uid));
            return Result.success(cards);
        }
        return Result.error("404","不存在银行卡");
    }

    @PostMapping("/getCard")//获取单张银行卡
    public Result getCard(@RequestBody Map map){
        Integer id=(Integer) map.get("cardid");
        QueryWrapper<BankCard> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("id","type","balance","cardnumber");
        BankCard card=bankCardMapper.selectOne(queryWrapper.eq("id",id));
        if(card!=null){
            return Result.success(card);
        }
        return Result.error("404","不存在银行卡");
    }

    @PostMapping("/returnloans")//还款贷款
    public Result returnloans(@RequestBody Map map){
        Integer rid=(Integer) map.get("rid");
        ReLoans reLoans=reLoansMapper.selectById(rid);
        LocalDate nowtime=LocalDate.now();
        if(nowtime.isBefore(reLoans.getTime())){
            return Result.error("500","尚未到还款日期，请耐心等待！");
        }
        BankCard bankCard=bankCardMapper.selectById(reLoans.getCid());
        if(bankCard.getBalance()<reLoans.getCost()){
            return Result.error("300","指定还款账户余额不足！");
        }
        if(setreturnmoney(bankCard,reLoans.getCost())){
            Integer lid=(Integer) map.get("lid");
            UserLoans userLoans=userLoansMapper.selectById(lid);
            userLoans.setNeedreturncost(userLoans.getNeedreturncost()-reLoans.getCost());//设置剩余金额
            userLoans.setLasttime(userLoans.getLasttime()-reLoans.getReturnday());//设置剩余期限
            reLoans.setReturntime(LocalDateTime.now());//设置还款日期
            if(nowtime.isAfter(reLoans.getTime())){//逾期但还款
                reLoans.setState(3);
            }
            else if(nowtime.isEqual(reLoans.getTime())){//按时归还
                reLoans.setState(1);
                //设置守信记录
                UserCredit userCredit=userCreditMapper.selectOne(Wrappers.<UserCredit>lambdaQuery().eq(UserCredit::getUid,userLoans.getUid()));
                userCredit.setKeeps(userCredit.getKeeps()+1);
                userCreditMapper.updateById(userCredit);
            }
            if(reLoansMapper.updateById(reLoans)==1){//提交成功，设置下个月的账单
                if(userLoans.getLasttime()!=0){//还有下一个月的账单
                    ReLoans reLoan=new ReLoans();
                    reLoan.setLid(reLoans.getLid());
                    reLoan.setCid(reLoans.getCid());
                    reLoan.setUid(reLoans.getUid());
                    reLoan.setTime(reLoans.getTime().plusMonths(1));
                    reLoan.setState(0);
                    Lender lender=lenderMapper.selectById(userLoans.getLid());
                    float rate=lender.getRate()/1200;//先获取贷款利息（月）
                    float cost=0;
                    float benjin=userLoans.getCost();//本金
                    float needreturncost=userLoans.getNeedreturncost();//还需归还本金
                    float time=userLoans.getTimelimit();//期限
                    if(userLoans.getReturntype()==1){//等额本息
                        cost=(float)( (benjin*rate*Math.pow(1+rate,time))/(Math.pow(1+rate,time)-1));
                    }
                    else if(userLoans.getReturntype()==2){//等额本金
                        cost=(benjin/time)+needreturncost*rate;
                    }
                    reLoan.setCost(cost);
                    reLoansMapper.insert(reLoan);
                }
                else if(userLoans.getLasttime()==0){//没有下一个月账单了
                    userLoans.setIspass(3);
                }
                userLoansMapper.updateById(userLoans);//更新
                return Result.success();
            }
        }
        return Result.error("404","系统错误！");
    }

    @PostMapping("/getCardsDetail")//获取银行卡的detail数组
    public Result getCardsDetail(@RequestBody Map map){
        Integer id=(Integer) map.get("cardsid");
        BankCard bankCard=bankCardMapper.selectById(id);
        if(bankCard!=null){
            List<Detail> details=JSONArray.parseArray(bankCard.getDetail(),Detail.class);
            List<Detail> list=new ArrayList<Detail>();
           if(details.size()>0){
               long begintime=(long) map.get("begintime");
               long endtime=(long) map.get("endtime")+86400000;
               for(int i=0;i<details.size();i++){
                   LocalDateTime time=details.get(i).getPaytime();
                   long timestamp=time.toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
                   if(timestamp>=begintime&&timestamp<=endtime){
                       list.add(details.get(i));
                   }
               }
               return Result.success(list);
           }
           else {
               return Result.error("300","暂无数据");
           }
        }
        return Result.error("404","不存在银行卡");
    }

    @PostMapping("/getCardsDetail2")
    public Result getCardsDetail2(@RequestBody Map map){
        Integer id=(Integer) map.get("cardsid");
        BankCard bankCard=bankCardMapper.selectById(id);
        bankCard.setDetail("[]");
        return Result.success(bankCard);
    }

    public boolean setreturnmoney(BankCard bankCard,float cost){//设置还款明细
        List<Detail> details=JSONArray.parseArray(bankCard.getDetail(),Detail.class);
        Detail detail=new Detail();
        detail.setId(IdGeneratorSnowlake.getInstance().snowflakeId());
        detail.setPaytime(LocalDateTime.now());
        detail.setPlace("网上银行");
        detail.setCost(cost);
        detail.setDescribe("归还贷款");
        detail.setType("贷款还款");
        details.add(detail);
        bankCard.setDetail(JSONArray.toJSONString(details));
        bankCard.setBalance(bankCard.getBalance()-cost);
        if(bankCardMapper.updateById(bankCard)==1){
            return true;
        }
        return false;
    }
}
