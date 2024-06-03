

package com.example.springbootbank.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootbank.common.CreateBankCards;
import com.example.springbootbank.common.IdGeneratorSnowlake;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.*;
import com.example.springbootbank.entity.otherEntity.AbnormalMsg;
import com.example.springbootbank.mapper.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/getusercards")//查询用户其下银行卡（分页）
    public Result getusercards(@RequestBody Map map){
        Integer pageNum=(Integer) map.get("currentPage") ;
        Integer pageSize=(Integer) map.get("pageSize") ;
        String search=(String) map.get("search") ;
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.ne("bankcards","[]").like("name",search);
        Page<User> userPage= userMapper.selectPage(new Page<>(pageNum, pageSize),queryWrapper);
        List<User> users=userPage.getRecords();
        List<Records> recordsList=new ArrayList<>();
        for(int i=0;i<users.size();i++){
            List<Cardids> cardidsList=JSONArray.parseArray(users.get(i).getBankcards(),Cardids.class);
            Records records=new Records();
            records.setName(users.get(i).getName());
            records.setId(users.get(i).getId());
            records.setPhone(users.get(i).getPhone());
            records.setAccount(users.get(i).getAccount());
            records.setCardsum(cardidsList.size());
            List<BankCard> bankCards=new ArrayList<>();
            for(int j=0;j<cardidsList.size();j++){
                BankCard bankCard=bankCardMapper.selectById(cardidsList.get(j).getId());
                bankCards.add(bankCard);
            }
            records.setBankCards(bankCards);
            recordsList.add(records);
        }
        JSONObject userpage=new JSONObject();
        userpage.put("total",userPage.getTotal());
        userpage.put("records",recordsList);
        return Result.success(userpage);
    }
    @PostMapping("/getcardsall")//查询银行卡（分页）
    public Result getcardsall(@RequestBody Map map){
        Integer pageNum=(Integer) map.get("currentPage") ;
        Integer pageSize=(Integer) map.get("pageSize") ;
        String search=(String) map.get("search") ;
        Integer state=(Integer) map.get("state"); //封禁状态
        Integer model=(Integer) map.get("model"); //模式
        if(!search.equals("")&&search!=null){//存在搜索限制
            QueryWrapper<User> queryWrapper=new QueryWrapper<>();
            List<User> users=userMapper.selectList(queryWrapper.like("name",search).ne("bankcards","[]"));
            if(model==1){//异常银行卡
                queryWrapper.eq("abnormal",1);
            }
            List<BankCard> bankCards=new ArrayList<>();
            for(int i=0;i<users.size();i++){
                List<Cardids> cardidsList=JSONArray.parseArray(users.get(i).getBankcards(),Cardids.class);
                for(int j=0;j<cardidsList.size();j++){
                    BankCard bankCard=bankCardMapper.selectById(cardidsList.get(j).getId());
                    if(bankCard.getState()==state){
                        bankCard.setName(users.get(i).getName());
                        bankCards.add(bankCard);
                    }
                }
            }
            List<BankCard> returncards=new ArrayList<>();
            Integer end=pageNum*pageSize;
            if(end>bankCards.size()){
                end=bankCards.size();
            }
            for(int i=(pageNum-1)*pageSize;i<end;i++){
                returncards.add(bankCards.get(i));
            }
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("total",bankCards.size());
            jsonObject.put("records",returncards);
            return Result.success(jsonObject);
        }
        else {
            QueryWrapper<BankCard> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("state",state);
            if(model==1){//异常银行卡
                queryWrapper.eq("abnormal",1);
            }
            Page<BankCard> userPage= bankCardMapper.selectPage(new Page<>(pageNum, pageSize),queryWrapper);
            for (int i=0;i<userPage.getRecords().size();i++){
                User user=userMapper.selectById( userPage.getRecords().get(i).getUid());
                userPage.getRecords().get(i).setName(user.getName());
            }
            return Result.success(userPage);
        }
    }
    //创建银行卡
    @PostMapping("/creatBankcards")//开通银行卡
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
        bankCard.setAbnormalmsg("[]");
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

    @PostMapping("/guashicard")//挂失银行卡
    public Result guashicard(@RequestBody Map map){
        Integer cid=(Integer) map.get("cid");
        Integer type=(Integer)map.get("type");
        BankCard bankCard=bankCardMapper.selectById(cid);
        if(type==1){
            bankCard.setLoststate(1);//挂失
            bankCard.setState(1);//封禁
        }
        else if(type==2){
            bankCard.setLoststate(0);//解除挂失
            bankCard.setState(0);//解除封禁
        }
        bankCardMapper.updateById(bankCard);
        return Result.success();
    }
    @PostMapping("/getCardsmore")//获取用户所有银行卡(详细版)
    public Result getCardsmore(@RequestBody Map map){
        Integer uid=(Integer) map.get("uid");
        User user=userMapper.selectById(uid);
        JSONArray jsonArray=JSONArray.parseArray(user.getBankcards());
        List <BankCard>cards=new ArrayList<BankCard>();
        if(jsonArray!=null){
            QueryWrapper<BankCard> queryWrapper=new QueryWrapper<>();
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
                userCredit.setKeeps(userCredit.getKeeps()+1);//按时归还增加守信记录
                userCredit.setCredit(userCredit.getCredit()+1);//按时归还信用分加1
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
    @PostMapping("/banCardbyId")//封禁银行卡
    public Result banCardbyId(@RequestBody Map map){
        Integer id=(Integer) map.get("cid");
        String msg=(String)map.get("msg");
        BankCard bankCard=bankCardMapper.selectById(id);
        bankCard.setState(1);//封禁
        bankCard.setStatedetail(msg);
        bankCardMapper.updateById(bankCard);
        return Result.success();
    }
    @PostMapping("/rebancard")//解开禁银行卡
    public Result rebancard(@RequestBody Map map){
        Integer id=(Integer) map.get("cid");
        BankCard bankCard=bankCardMapper.selectById(id);
        bankCard.setState(0);//解禁
        bankCard.setRestate(0);
        bankCardMapper.updateById(bankCard);
        return Result.success();
    }
    @PostMapping("/rebancardsatte")//申请解禁银行卡
    public Result rebancardsatte(@RequestBody Map map){
        Integer id=(Integer) map.get("cid");
        BankCard bankCard=bankCardMapper.selectById(id);
        bankCard.setRestate(1);//解禁申请
        bankCardMapper.updateById(bankCard);
        return Result.success();
    }
    @PostMapping("/clearerror")//清除银行卡异常
    public Result clearerror(@RequestBody Map map){
        Integer id=(Integer) map.get("cid");
        BankCard bankCard=bankCardMapper.selectById(id);
        bankCard.setAbnormal(0);//解除异常
        List<AbnormalMsg> abnormalMsgs=JSONArray.parseArray(bankCard.getAbnormalmsg(), AbnormalMsg.class);
        for(int i=0;i<abnormalMsgs.size();i++){
            if(abnormalMsgs.get(i).getState()==1){
                abnormalMsgs.get(i).setState(0);
            }
        }
        bankCard.setAbnormalmsg(JSONArray.toJSONString(abnormalMsgs));
        bankCardMapper.updateById(bankCard);
        return Result.success();
    }
    @PostMapping("/banorreBanCardbyId")//批量解禁或者封禁银行卡
    public Result banorreBanCardbyId(@RequestBody Map map){
        List<Integer> ids=(List<Integer>) map.get("ids");
        Integer type=(Integer) map.get("type");
        for(int i=0;i<ids.size();i++){
            BankCard bankCard=bankCardMapper.selectById(ids.get(i));
            bankCard.setState(type);
            if(type==0){
                bankCard.setRestate(0);
            }
            bankCardMapper.updateById(bankCard);
        }
        return Result.success();
    }
    @PostMapping("/getabnormoldetail")//获取银行卡的异常操作
    public Result getabnormoldetail(@RequestBody Map map){
        Integer id=(Integer) map.get("cid");
        Integer type=(Integer)map.get("type");
        BankCard bankCard=bankCardMapper.selectById(id);
        List<AbnormalMsg> abnormalMsgList=JSONArray.parseArray(bankCard.getAbnormalmsg(), AbnormalMsg.class);
        List<AbnormalMsg> returnmsg=new ArrayList<>();
        for(int i=abnormalMsgList.size()-1;i>=0;i--){
            if(abnormalMsgList.get(i).getState()==type){//正生效的
                Integer flag=0;
                for(int j=0;j<returnmsg.size();j++){
                    if(returnmsg.get(j).getType()==abnormalMsgList.get(i).getType()){//重复了
                        flag=1;
                        break;
                    }
                }
                if(flag==0){
                    returnmsg.add(0,abnormalMsgList.get(i));
                }
            }
        }
        return Result.success(returnmsg);
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
@Data//银行卡id
class Cardids{
    private Integer id;
}
@Data//用户其下银行卡的返回数据
class Records{
    private Integer id;
    private String name;
    private String account;
    private String phone;
    private Integer cardsum;
    private List<BankCard> bankCards;
}