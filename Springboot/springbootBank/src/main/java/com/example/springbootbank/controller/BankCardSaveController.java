package com.example.springbootbank.controller;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.BankCard;
import com.example.springbootbank.entity.Detail;
import com.example.springbootbank.entity.User;
import com.example.springbootbank.entity.otherEntity.AbnormalMsg;
import com.example.springbootbank.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/BankCardSave")
public class BankCardSaveController {
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


    @PostMapping("/transfertest")//转账警告设置安全检验
    public Result transfertest(@RequestBody Map map){
        String cardnumber=(String)map.get("paycard");//接收方账号
        String name=(String)map.get("payname");//接收方姓名
        BankCard bankCard=bankCardMapper.selectOne(Wrappers.<BankCard>lambdaQuery().eq(BankCard::getCardnumber,cardnumber));
        Integer uid1=bankCard.getUid();
        if(bankCard!=null){
            User user=userMapper.selectById(bankCard.getUid());
            if(user.getName().equals(name)){//验证成功
                Integer id=(Integer) map.get("cardid") ;//银行卡id
                //检查是不是自己的卡
                BankCard bankCard1=bankCardMapper.selectById(id);
                Integer uid2=bankCard1.getUid();
                if(uid1==uid2){//是自己的卡
                    return Result.success(0);
                }
                List<Detail> bankcardDetails= JSONArray.parseArray(bankCard1.getDetail(),Detail.class);
                LocalDateTime starttime=LocalDateTime.now().minusDays(1);
                Integer sum=0;
                for(int i=0;i<bankcardDetails.size();i++){
                    if(bankcardDetails.get(i).getType().equals("转账转出")&&bankcardDetails.get(i).getPaytime().isAfter(starttime)){
                        if(bankcardDetails.get(i).getReciaccount().equals(cardnumber)){
                            sum++;
                        }
                    }
                }
                if(sum>=5){//24小时内对同一个用户转账超过5次
                    bankCard1.setAbnormal(1);
                    AbnormalMsg abnormalMsg=new AbnormalMsg();
                    List<AbnormalMsg> abnormalMsgs=JSONArray.parseArray(bankCard1.getAbnormalmsg(), AbnormalMsg.class);
                    abnormalMsg.setTime(LocalDateTime.now());
                    abnormalMsg.setDescription("24小时内对同一账户："+cardnumber+"账号转账超过"+sum+"次！");
                    abnormalMsg.setState(1);
                    abnormalMsg.setType(2);//转账次数过多
                    abnormalMsgs.add(abnormalMsg);
                    bankCard1.setAbnormalmsg(JSONArray.toJSONString(abnormalMsgs));
                    bankCardMapper.updateById(bankCard1);
                }
                return Result.success(sum);
            }
            return Result.error("500","错误！接收方姓名或银行卡号错误，请重新填写");
        }
        return Result.error("404","错误！接收方账户不存在，请重新填写");
    }

    @PostMapping("/getttime")//获取转账次数
    public Result getttime(@RequestBody Map map){
        String cardnumber=(String)map.get("paycard");//接收方账号
        String name=(String)map.get("payname");//接收方姓名
        BankCard bankCard=bankCardMapper.selectOne(Wrappers.<BankCard>lambdaQuery().eq(BankCard::getCardnumber,cardnumber));
        Integer uid1=bankCard.getUid();
        if(bankCard!=null){
            User user=userMapper.selectById(bankCard.getUid());
            if(user.getName().equals(name)){//验证成功
                Integer id=(Integer) map.get("cardid") ;//银行卡id
                //检查是不是自己的卡
                BankCard bankCard1=bankCardMapper.selectById(id);
                Integer uid2=bankCard1.getUid();
                if(uid1==uid2){//是自己的卡
                    return Result.success(0);
                }
                List<Detail> bankcardDetails= JSONArray.parseArray(bankCard1.getDetail(),Detail.class);
                LocalDateTime starttime=LocalDateTime.now().minusDays(1);
                Integer sum=0;
                for(int i=0;i<bankcardDetails.size();i++){
                    if(bankcardDetails.get(i).getType().equals("转账转出")&&bankcardDetails.get(i).getPaytime().isAfter(starttime)){
                        if(bankcardDetails.get(i).getReciaccount().equals(cardnumber)){
                            sum++;
                        }
                    }
                }
                return Result.success(sum);
            }
            return Result.error("500","错误！接收方姓名或银行卡号错误，请重新填写");
        }
        return Result.error("404","错误！接收方账户不存在，请重新填写");
    }

    @PostMapping("/costsave")//日流水监控(监控转账转出、消费、取出)
    public Result costsave(@RequestBody Map map){
        Integer cid=(Integer) map.get("cid");
        BankCard bankCard=bankCardMapper.selectById(cid);
        List<Detail> details=JSONArray.parseArray(bankCard.getDetail(),Detail.class);
        LocalDateTime now=LocalDateTime.now().minusDays(1);
        float cost=0;
        for(int i=0;i<details.size();i++){
            if(details.get(i).getPaytime().isAfter(now)){
                if(details.get(i).getType().equals("转账转出")||details.get(i).getType().equals("消费")||details.get(i).equals("取出")){
                    cost=cost+details.get(i).getCost();
                }
            }
        }
        if(cost>=20000){
            bankCard.setAbnormal(1);
            List<AbnormalMsg> abnormalMsgList=JSONArray.parseArray(bankCard.getAbnormalmsg(), AbnormalMsg.class);
            AbnormalMsg abnormalMsg=new AbnormalMsg();
            abnormalMsg.setState(1);
            abnormalMsg.setType(1);//超额
            abnormalMsg.setTime(now);
            abnormalMsg.setDescription("24小时内银行卡流水（用于消费、转账、取出）超过2万元，日流水高达："+cost+"元");
            abnormalMsgList.add(abnormalMsg);
            bankCard.setAbnormalmsg(JSONArray.toJSONString(abnormalMsgList));
            bankCardMapper.updateById(bankCard);
            return Result.success(cost);
        }
        return Result.error("500","无风险");
    }
}
