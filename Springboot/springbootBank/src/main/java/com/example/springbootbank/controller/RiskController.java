package com.example.springbootbank.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.*;
import com.example.springbootbank.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/Risk")
public class RiskController {
    @Resource

    @Autowired
    BankCardMapper bankCardMapper;
    @Autowired
    UserMapper userMapper;


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
    UserInfoMapper userInfoMapper;

    @Autowired
    UserCreditMapper userCreditMapper;

    @PostMapping("/getriskM")//获取用户表和用户风险等级表
    public Result getriskM(@RequestBody Map maps){
        List<UserCredit > userCredits=userCreditMapper.selectList(null);
        Map<Integer,Integer> map=new HashMap<>();
        for(int i=0;i<userCredits.size();i++){
            Integer risk=userCredits.get(i).getRisk();
            map.put(risk,map.get(risk)==null? 1 : map.get(risk)+1);
        }
        return Result.success(map);
    }
    @PostMapping("/getriskOne")//获取单个用户风险得分
    public Result getriskOne(@RequestBody Map map){
        Integer uid=(Integer) map.get("uid");
        UserCredit userCredit=userCreditMapper.selectOne(Wrappers.<UserCredit>lambdaQuery().eq(UserCredit::getUid,uid));
        return Result.success(userCredit.getRisk());
    }
    @PostMapping("/riskanalysis")//风险分析
    public Result getLoans(@RequestBody Map map){
        Integer type=(Integer) map.get("type"); //分析类型，1为全部用户进行风险分析，2为只对使用系统的用户分析，3为指定某个用户进行分析
        Integer id=(Integer)map.get("id");//用户id
        double r1=(double) map.get("r1");
        double r2=(double) map.get("r2");
        double r3=(double) map.get("r3");
        double r4=(double) map.get("r4");
        if(type==1){
            List<UserCredit> userCredits=userCreditMapper.selectList(null);
            for(int i=0;i<userCredits.size();i++){
                if(!userriskanalysis(userCredits.get(i),r1,r2,r3,r4)){
                    return Result.error("500","分析途中出现错误，请稍后再试");
                }
            }
        }
        else if(type==2){
            QueryWrapper<User> queryWrapper=new QueryWrapper<>();
            List<User> users=userMapper.selectList(queryWrapper.ne("bankcards","[]"));
            for(int i=0;i<users.size();i++){
                UserCredit userCredit=userCreditMapper.selectOne(Wrappers.<UserCredit>lambdaQuery().eq(UserCredit::getUid,users.get(i).getId()));
                if(!userriskanalysis(userCredit,r1,r2,r3,r4)){
                    return Result.error("500","分析途中出现错误，请稍后再试");
                }
            }
        }
        else if(type==3){
            UserCredit userCredit=userCreditMapper.selectOne(Wrappers.<UserCredit>lambdaQuery().eq(UserCredit::getUid,id));
            if(!userriskanalysis(userCredit,r1,r2,r3,r4)){
                return Result.error("500","分析途中出现错误，请稍后再试");
            }
        }
        return Result.success();
    }

    public boolean userriskanalysis(UserCredit userCredit,double r1,double r2,double r3,double r4){//对单个用户进行分析
        //r1为储蓄、r2为信用、3为贷款、4为年龄
        Integer id=userCredit.getUid();
        UserInfo userInfo=userInfoMapper.selectOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUid,id));
        if(userInfo!=null){
            double code=balance(id)*r1+userCredit.getCredit()*r2+loanscode(userInfo)*r3+agecode(userInfo)*r4;
            int codes=(int)Math.round(code);
            userCredit.setRisk(codes);
            if(userCreditMapper.updateById(userCredit)==1){
                return true;
            }
        }
        //风险分析算法
        if(userInfo==null){
            return true;
        }
        return false;
    }
    public int loanscode(UserInfo userInfo){//计算贷款得分（是否有放贷或者其他贷款）
        int code=100;
        if(userInfo.getHousing().equals("yes")){
            code=code-30;
        }
        if(userInfo.getLoan().equals("yes")){
            code=code-20;
        }
        return code;
    }
    public int balance(int id ){//计算储蓄得分
        int code=60;
        float balance=0;
        QueryWrapper<BankCard> queryWrapper=new QueryWrapper<>();
        List<BankCard> bankCardList=bankCardMapper.selectList(queryWrapper.eq("uid",id).ne("type",3));
        if(bankCardList.size()>0){
            for(int i=0;i<bankCardList.size();i++){
                balance=balance+bankCardList.get(i).getBalance();
            }
            if(balance>=10000&&balance<100000){
                code=code+10;
            }
            else if(balance>=100000&&balance<200000){
                code=code+15;
            }
            else if(balance>=200000&&balance<500000){
                code=code+20;
            }
            else if(balance>=500000&&balance<1000000){
                code=code+30;
            }
            else if(balance>=1000000){
                code=code+35;
            }
        }
        return code;
    }
    public int agecode(UserInfo userInfo){//计算用户年龄得分
        int code=60;
        int age=userInfo.getAge();
        //年龄在23-50岁之间加分
        if(age>=23&&age<30){
            code=code+5;
        }
        else if(age>=30&&age<=40){
            code=code+10;
        }
        else if(age>40&&age<50){
            code=code+3;
        }
        else if(age>=50&&age<60){
            code=code-5;
        }
        else if(age>=60&&age<70){
            code=code-10;
        }
        else if(age>=70){
            code=code-20;
        }
        return code;
    }
}
