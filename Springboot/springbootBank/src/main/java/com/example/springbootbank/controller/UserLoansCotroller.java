package com.example.springbootbank.controller;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.Files;
import com.example.springbootbank.entity.UserLoans;
import com.example.springbootbank.entity.otherEntity.PassMsg;
import com.example.springbootbank.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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




    @PostMapping("/sentLoans")//提交贷款申请
    public Result getMarketDatebyId(@RequestBody Map map){
        System.out.println(map);
        UserLoans userLoans=new UserLoans();
        //开始创建
        userLoans.setCid((Integer) map.get("cid"));
        userLoans.setLid((Integer) map.get("lid"));
        userLoans.setUid((Integer) map.get("uid"));
        userLoans.setCost(Float.valueOf((String)map.get("cost"))*10000);
        userLoans.setReturntype((Integer) map.get("returnType"));
        userLoans.setTimelimit(Integer.parseInt((String) map.get("timelimit")));
        userLoans.setSalary(Float.valueOf((String)map.get("salary")));
        ArrayList<Integer> fileList=(ArrayList<Integer>) map.get("fileList");//证明材料的文件id
        //创建审核信息
        PassMsg passMsg=new PassMsg();
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
    @PostMapping("/getLoans")//获取贷款申请
    public Result getLoans(@RequestBody Map map){
        Integer pageNum=(Integer) map.get("pageNum");
        Integer pageSize=(Integer) map.get("pageSize");
        Integer Ispass=(Integer) map.get("ispass");
        Page<UserLoans> page= Page.of(pageNum,pageSize);
        LambdaQueryWrapper<UserLoans> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(UserLoans::getIspass,Ispass);
        userLoansMapper.selectPage(page,queryWrapper);
        return Result.success(page);
    }
}
