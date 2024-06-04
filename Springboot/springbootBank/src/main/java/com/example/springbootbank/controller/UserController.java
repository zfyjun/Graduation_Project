package com.example.springbootbank.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.User;
import com.example.springbootbank.entity.UserCredit;
import com.example.springbootbank.entity.UserInfo;
import com.example.springbootbank.entity.UserLoans;
import com.example.springbootbank.mapper.UserCreditMapper;

import com.example.springbootbank.mapper.UserInfoMapper;
import com.example.springbootbank.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/User")
public class UserController {
    @Resource
    private UserInfoMapper userInfoMapper;


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserCreditMapper userCreditMapper;
    @PostMapping ("/login")
    public Result login(@RequestBody Map map){
        String account=(String)map.get("account") ;
        String password=(String) map.get("password") ;
        User user=userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getAccount,account));
        if(user!=null){
            if(password.equals(String.valueOf(user.getPassword())) ){

                return Result.success(user);
            }
            return Result.error("500","密码错误！");
        }
        return Result.error("404","账户不存在！");
    }

    @PostMapping ("/setLoginTime")
    public Result setLoginTime(@RequestBody Map map){
        Integer id=(Integer)map.get("id") ;
        User user=userMapper.selectById(id);
        DateTimeFormatter df=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now=LocalDateTime.now();
        String time=df.format(now);
        now=LocalDateTime.parse(time,df);
        user.setLasttime(now);
        userMapper.updateById(user);
       return Result.success();
    }

    @PostMapping ("/changePassword")
    public Result changePassword(@RequestBody Map map){
        Integer uid=(Integer)map.get("uid") ;
        Integer password= Integer.valueOf( (String)map.get("newpassword"));
        System.out.println("uid:"+uid+"  password:"+password);
        User user=userMapper.selectById(uid);
        user.setPassword(password);
        Integer flage=userMapper.updateById(user);
        if(flage==1){
            return Result.success();
        }
        return Result.error("500","密码更新失败！");
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String name) {
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.like("name",name);
//        Page<User> page = new Page<>(pageNum, pageSize);
        return Result.success(userMapper.selectPage(new Page<>(pageNum, pageSize),queryWrapper));
    }


    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id){
        return Result.success(userMapper.deleteById(id));
    }

    @PostMapping
    public Result save(@RequestBody User user){
        System.out.println(user);
        userMapper.insert(user);
        String account=user.getAccount();
        User one=userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getAccount,account));
        UserInfo userInfo=new UserInfo();
        userInfo.setUid(one.getId());
        System.out.println(userInfo);
        userInfoMapper.insert(userInfo);
        return Result.success();
    }

    @PostMapping("/update")
    public Result update(@RequestBody User user){
        return Result.success(userMapper.updateById(user));
    }

    @PostMapping("/getUserRisk")//获取用户表和用户风险等级表
    public Result getLoans(@RequestBody Map map){
        Integer pageNum=(Integer) map.get("pageNum");
        Integer pageSize=(Integer) map.get("pageSize");
        String name=(String) map.get("name");
        Page<User> page= Page.of(pageNum,pageSize);
        LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(User::getName,name);
        userMapper.selectPage(page,queryWrapper);
        for(int i=0;i<page.getRecords().size();i++){
            UserCredit userCredit=userCreditMapper.selectById(page.getRecords().get(i).getId());
            if(userCredit!=null){
                page.getRecords().get(i).setRisk(userCredit.getRisk());
            }
        }
        return Result.success(page);
    }

    @PostMapping("/getUserinfo")//获取用户详情信息
    public Result getUserinfo(@RequestBody Map map){
        Integer uid=(Integer)map.get("uid");
        UserInfo userInfo=userInfoMapper.selectOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUid,uid));
        return Result.success(userInfo);
    }
    @PostMapping("/getUser")//获取用户信息
    public Result getUser(@RequestBody Map map){
        Integer uid=(Integer)map.get("uid");
        User user=userMapper.selectById(uid);
        return Result.success(user);
    }
    @PostMapping("/setUserinfoanuser")//修改用户信息
    public Result setUserinfoanuser(@RequestBody Map map){
        User user= JSONObject.parseObject((String) map.get("user"),User.class);
        UserInfo userInfo= JSONObject.parseObject((String)map.get("userinfo"),UserInfo.class) ;
        QueryWrapper<User> queryWrapper1=new QueryWrapper<>();
        List<User> userListidcards=userMapper.selectList(queryWrapper1.eq("idcard",user.getIdcard()));
        for(int i=0;i<userListidcards.size();i++){
            if(userListidcards.get(i).getId()!=user.getId()){
                return Result.error("500","错误！该身份证号码已被注册！");
            }
        }
        QueryWrapper<User> queryWrapper2=new QueryWrapper<>();
        List<User> userListphone=userMapper.selectList(queryWrapper2.eq("phone",user.getPhone()));
        for(int i=0;i<userListphone.size();i++){
            if(userListphone.get(i).getId()!=user.getId()){
                return Result.error("500","错误！该电话号码已被注册！");
            }
        }
        userMapper.updateById(user);
        userInfoMapper.updateById(userInfo);
        return Result.success();
    }
}
