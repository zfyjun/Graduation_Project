package com.example.springbootbank.controller;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

@RestController
@RequestMapping("/UserLoans")
public class UserLoansCotroller {
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
    UserMapper userMapper;

    @Autowired
    ReLoansMapper reLoansMapper;
    @PostMapping("/sentLoans")//提交贷款申请
    public Result getMarketDatebyId(@RequestBody Map map){
        System.out.println(map);
        UserLoans userLoans=new UserLoans();
        //开始创建
        userLoans.setCid((Integer) map.get("cid"));
        BankCard bankCard=bankCardMapper.selectById((Integer) map.get("cid"));
        if(bankCard.getState()!=0){
            return Result.error("500","该银行卡处于封禁状态！无法进行如何操作");
        }
        userLoans.setLid((Integer) map.get("lid"));
        userLoans.setUid((Integer) map.get("uid"));
        userLoans.setCost(Float.valueOf((String)map.get("cost"))*10000);
        userLoans.setReturntype((Integer) map.get("returnType"));
        userLoans.setTimelimit(Integer.parseInt(String.valueOf(map.get("timelimit")) ));
        userLoans.setSalary(Float.valueOf((String)map.get("salary")));
        ArrayList<Integer> fileList=(ArrayList<Integer>) map.get("fileList");//证明材料的文件id
        //创建审核信息
        PassMsg passMsg=new PassMsg();
        passMsg.setId(IdGeneratorSnowlake.getInstance().snowflakeId());
        LocalDateTime now=LocalDateTime.now();
        passMsg.setSenttime(now);
        passMsg.setState(0);//审核中
        List<PassMsg> passMsgList=new ArrayList<>();
        passMsgList.add(passMsg);
        userLoans.setPassmsg(JSONArray.toJSONString(passMsgList));
        userLoans.setMaterials(JSONArray.toJSONString(fileList));
        if(userLoansMapper.insert(userLoans)==1){
            return Result.success();
        }
        return Result.error("500","贷款申请提交失败");
    }
    @PostMapping("/getLoans")//根据贷款申请进度状态获取贷款申请
    public Result getLoans(@RequestBody Map map){
        Integer pageNum=(Integer) map.get("pageNum");
        Integer pageSize=(Integer) map.get("pageSize");
        Integer Ispass=(Integer) map.get("ispass");
        Page<UserLoans> page= Page.of(pageNum,pageSize);
        LambdaQueryWrapper<UserLoans> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(UserLoans::getIspass,Ispass);
        userLoansMapper.selectPage(page,queryWrapper);
        for(int i=0;i<page.getRecords().size();i++){
            User user=userMapper.selectById(page.getRecords().get(i).getUid());
            page.getRecords().get(i).setUsername(user.getName());
        }
        return Result.success(page);
    }
    @PostMapping("/getLoansByUserid")//根据用户id获取某个用户的贷款申请信息
    public Result getLoansByUserid(@RequestBody Map map){
        Integer uid=(Integer) map.get("uid");
        QueryWrapper<UserLoans> queryWrapper=new QueryWrapper<>();
        List<UserLoans>userLoansList=userLoansMapper.selectList(queryWrapper.eq("uid",uid));
        if(userLoansList.size()>0){
            return Result.success(userLoansList);
        }
        return Result.error("404","该用户暂无贷款申请");
    }
    @PostMapping("/setloans")//更改贷款信息状态（）1未通过，2通过
    public Result setloans(@RequestBody Map map){
        Integer id=(Integer) map.get("id");
        String msg=(String) map.get("msg");
        Integer type=(Integer) map.get("type");
        UserLoans userLoans=userLoansMapper.selectById(id);
        List<PassMsg> detailspay= JSONArray.parseArray(userLoans.getPassmsg(),PassMsg.class);
        detailspay.get(detailspay.size()-1).setState(type);
        detailspay.get(detailspay.size()-1).setRespondtime(LocalDateTime.now());
        detailspay.get(detailspay.size()-1).setDescription(msg);
        userLoans.setIspass(type);
        userLoans.setPassmsg(JSONArray.toJSONString(detailspay));
        userLoans.setNeedreturncost(userLoans.getCost());
        userLoans.setLasttime(userLoans.getTimelimit());
        if(type==1){//未通过
            if(userLoansMapper.updateById(userLoans)==1){
                return Result.success();
            }
        }
        else if(type==2){//通过
            userLoans.setWorktime(LocalDate.now());
            BankCard bankCard=bankCardMapper.selectById(userLoans.getCid());
            if(bankCard.getState()!=0){
                return Result.error("500","该银行卡处于封禁状态！无法通过申请");
            }
            if(newreturnpay(userLoans)){
                if(addloans(userLoans)&&userLoansMapper.updateById(userLoans)==1){
                    return Result.success();
                }
            }
        }
        return Result.success();
    }

    public boolean addloans (UserLoans userLoans){//银行卡贷款转账（申请通过后进行）
        BankCard bankCard=bankCardMapper.selectById(userLoans.getCid());
        bankCard.setBalance(bankCard.getBalance()+userLoans.getCost());//给用户银行卡添加账户的钱
        List<Detail> details= JSONArray.parseArray(bankCard.getDetail(),Detail.class);//获取详情
        Detail detail=new Detail();
        detail.setId(IdGeneratorSnowlake.getInstance().snowflakeId());
        detail.setPaytime(LocalDateTime.now());
        detail.setCost(userLoans.getCost());
        detail.setType("贷款转入");
        detail.setPlace("网上银行");
        details.add(detail);
        bankCard.setDetail(JSONArray.toJSONString(details));
        //设置下个月还款账单
        if(bankCardMapper.updateById(bankCard)==1){
            return true;
        }
        return false;
    }
    //给通过的贷款创建一个还款账单
    public boolean newreturnpay(UserLoans userLoans){
        Lender lender=lenderMapper.selectById(userLoans.getLid());
        float rate=lender.getRate()/1200;//先获取贷款利息（月）
        ReLoans reLoans=new ReLoans();
        reLoans.setTime(userLoans.getWorktime().plusMonths(1));//设置下个月还款时间
        reLoans.setLid(userLoans.getId());
        reLoans.setCid(userLoans.getCid());
        reLoans.setUid(userLoans.getUid());
        //设置还款花费
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
        cost=Math.round(cost*100)/100f;
        reLoans.setCost(cost);
        if(reLoansMapper.insert(reLoans)==1){
            return true;
        }
        return false;
    }
}
