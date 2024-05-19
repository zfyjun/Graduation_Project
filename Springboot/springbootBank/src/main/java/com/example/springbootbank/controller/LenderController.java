package com.example.springbootbank.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.*;
import com.example.springbootbank.entity.otherEntity.PassMsg;
import com.example.springbootbank.mapper.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/Lender")
public class LenderController {
    @Resource

    @Autowired
    BankCardMapper bankCardMapper;

    @Autowired
    ProductMapper productMapper;

    @Autowired
    UserMapper userMapper;
    @Autowired
    UserProductMapper userproductMapper;

    @Autowired
    EnterpriseMapper enterpriseMapper;

    @Autowired
    UserProductMapper userProductMapper;

    @Autowired
    LenderMapper lenderMapper;
    @Autowired
    UserCreditMapper userCreditMapper;

    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    UserLoansMapper userLoansMapper;
    @Resource
    @Autowired
    FilesMapper filesMapper;
    @PostMapping("/getLenders")//获取全部贷款类型
    public Result getLenders(@RequestBody Map map){
        List<Lender> lenders=lenderMapper.selectList(null);
        if(lenders.size()>0){
            return Result.success(lenders);
        }
        return Result.error("500","暂无贷款产品信息");
    }
    @PostMapping("/getonemsg")//获取单人的贷款详细信息
    public Result getonemsg(@RequestBody Map map){
        Integer id=(Integer) map.get("id");
        UserInfo userInfo=userInfoMapper.selectOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUid,id));
        UserCredit userCredit=userCreditMapper.selectOne(Wrappers.<UserCredit>lambdaQuery().eq(UserCredit::getUid,id));
        UserLoans userLoans=userLoansMapper.selectOne(Wrappers.<UserLoans>lambdaQuery().eq(UserLoans::getUid,id));
        Returnmsg returnmsg=new Returnmsg();
        returnmsg.setAge(userInfo.getAge());
        returnmsg.setId(userLoans.getId());
        returnmsg.setDefaults(userCredit.getDefaults());
        returnmsg.setMarital(userInfo.getMarital());
        List<PassMsg> passMsgList= JSONArray.parseArray(userLoans.getPassmsg(),PassMsg.class);
        returnmsg.setSenttime(passMsgList.get(passMsgList.size()-1).getSenttime());
        List<Integer> integers=JSONArray.parseArray(userLoans.getMaterials(),Integer.class);
        List <String> urls=new ArrayList<>();
        for(int i=0;i<integers.size();i++){
            Files files=filesMapper.selectById(integers.get(i));
            urls.add(files.getUrl());
        }
        returnmsg.setUrls(urls);
        returnmsg.setRisk(userCredit.getRisk());
        return Result.success(returnmsg);
    }
}
@Data
class Returnmsg{
    LocalDateTime senttime;
    Integer id;
    Integer age;
    Integer defaults;
    String marital;
    Integer risk;
    List <String> urls;
}
