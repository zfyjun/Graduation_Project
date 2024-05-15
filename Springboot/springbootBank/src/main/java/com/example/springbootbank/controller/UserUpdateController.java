package com.example.springbootbank.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.springbootbank.common.IdGeneratorSnowlake;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.*;
import com.example.springbootbank.entity.otherEntity.PassMsg;
import com.example.springbootbank.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/UserUpdate")
public class UserUpdateController {
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
    @Autowired
    CreditCardMapper creditCardMapper;
    @Autowired
    UserMapper userMapper;

    @Autowired
    UserUpdateMapper userUpdateMapper;

    @PostMapping("/requestCard")//申请信用卡升级申请审核内容修改（审核）
    public Result requestCard(@RequestBody Map map){
        Integer id=(Integer) map.get("id");
        Integer type=(Integer) map.get("type");//通过与否
        String msg=(String) map.get("msg");//解释
        UserUpdate userUpdate=userUpdateMapper.selectById(id);
        if(userUpdate!=null){
            userUpdate.setState(type);
            List<PassMsg> passMsgList= JSONArray.parseArray(userUpdate.getPassmsg(),PassMsg.class);
            LocalDateTime senttime;
            for(int i=passMsgList.size()-1;i>=0;i--){
                if(passMsgList.get(i).state==0){//获取最后一次提交审核的申请
                    senttime=passMsgList.get(i).getSenttime();//
                    PassMsg passMsg=new PassMsg();
                    passMsg.setDescription(msg);
                    passMsg.setState(type);
                    passMsg.setRespondtime(LocalDateTime.now());
                    passMsg.setSenttime(senttime);
                    passMsg.setId(IdGeneratorSnowlake.getInstance().snowflakeId());
                    passMsgList.add(passMsg);
                    break;
                }
            }
            userUpdate.setPassmsg(JSONArray.toJSONString(passMsgList));
            if(userUpdateMapper.updateById(userUpdate)==1){
                return Result.success();
            }
        }
        return Result.error("404","找到不到当前审核申请内容");
    }
    @PostMapping("/resent")//信用卡申请重新提交
    public Result resent(@RequestBody Map map){
        Integer id=(Integer) map.get("id");
        UserUpdate userUpdate=userUpdateMapper.selectById(id);
        if(userUpdate!=null){
            float cost=Float.valueOf(String.valueOf(map.get("cost")));//额度
            Integer rank=(Integer) map.get("rank");//等级
            PassMsg passMsg=new PassMsg();
            passMsg.setId(IdGeneratorSnowlake.getInstance().snowflakeId());
            passMsg.setState(0);
            passMsg.setSenttime(LocalDateTime.now());
            List<PassMsg> passMsgList=new ArrayList<>();
            passMsgList.add(passMsg);
            userUpdate.setPassmsg(JSONArray.toJSONString(passMsgList));
            userUpdate.setRank(rank);
            userUpdate.setCost(cost);
            if(userUpdateMapper.insert(userUpdate)==1){
                return Result.success();
            }
        }
        return Result.error("404","找不到该信用卡升级提额申请记录");
    }

    @PostMapping("/sentupdate")//申请信用卡升级提额
    public Result getBill(@RequestBody Map map){
        Integer cid=(Integer) map.get("cid");
        Integer uid=(Integer) map.get("uid");
        Integer type=(Integer) map.get("type");//类型
        QueryWrapper<UserUpdate> queryWrapper=new QueryWrapper<>();
        List<UserUpdate> userUpdates=userUpdateMapper.selectList(queryWrapper.eq("cid",cid).eq("uid",uid));
        for(int i=0;i<userUpdates.size();i++){
            if(userUpdates.get(i).getState()==0){//该卡还有申请在审核中
                return Result.error("500","该卡已经提交过升级或者额度提升的审核请求，请等待审核通过后进行操作！");
            }
        }
        float cost=Float.valueOf(String.valueOf(map.get("cost")));//额度
        Integer rank=(Integer) map.get("rank");//等级
        PassMsg passMsg=new PassMsg();
        passMsg.setId(IdGeneratorSnowlake.getInstance().snowflakeId());
        passMsg.setState(0);
        passMsg.setSenttime(LocalDateTime.now());
        List<PassMsg> passMsgList=new ArrayList<>();
        passMsgList.add(passMsg);
        ////
        UserUpdate userUpdate=new UserUpdate();
        userUpdate.setCid(cid);
        userUpdate.setType(type);
        userUpdate.setCost(cost);
        userUpdate.setRank(rank);
        userUpdate.setPassmsg(JSONArray.toJSONString(passMsgList));
        userUpdate.setUid(uid);
        if(userUpdateMapper.insert(userUpdate)==1){
            return Result.success();
        }
        return Result.error("404","操作失败");
    }

    @PostMapping("/getUpdatebyType")//申请信用卡升级提额
    public Result getUpdatebyType(@RequestBody Map map){
        Integer type=(Integer) map.get("type");
        QueryWrapper<UserUpdate> queryWrapper=new QueryWrapper<>();
        List<UserUpdate> userUpdates=userUpdateMapper.selectList(queryWrapper.eq("type",type));
        if(userUpdates.size()>0){
            List<JSONObject> jsonObjects=new ArrayList<>();
            for(int i=0;i<userUpdates.size();i++){
                if(userUpdates.get(i).getState()==0){
                    JSONObject jsonObject=new JSONObject();
                    jsonObject.put("update",userUpdates.get(i));//申请信息
                    User user=userMapper.selectById(userUpdates.get(i).getUid());
                    jsonObject.put("name",user.getName());//用户姓名
                    //获取信用卡相关信息
                    CreditCard creditCard=creditCardMapper.selectOne((Wrappers.<CreditCard>lambdaQuery().eq(CreditCard::getCid,userUpdates.get(i).getCid())));
                    List<Debt> Debts= JSONArray.parseArray(creditCard.getDebt(),Debt.class);
                    Integer defaults=0;
                    List<LocalDate> localDates=new ArrayList<>();
                    for(int j=0;j<Debts.size();j++){
                        if(Debts.get(j).getDays()>0){
                            defaults=defaults+1;
                            LocalDate time=Debts.get(i).getTimelast();
                            localDates.add(time);
                        }
                    }
                    jsonObject.put("defaults",defaults);
                    jsonObject.put("times",localDates);
                    jsonObject.put("rank",creditCard.getRank());
                    jsonObject.put("limits",creditCard.getLimits());
                    List<PassMsg> passMsgList= JSONArray.parseArray(userUpdates.get(i).getPassmsg(),PassMsg.class);
                    for(int j=passMsgList.size()-1;j>=0;j--){
                        if(passMsgList.get(j).getState()==0){
                            jsonObject.put("senttime",passMsgList.get(j).getSenttime());
                            break;
                        }
                    }
                    jsonObjects.add(jsonObject);
                }
            }
            if(jsonObjects.size()>0){
                return Result.success(jsonObjects);
            }
        }
        return Result.error("404","无数据");
    }
}
