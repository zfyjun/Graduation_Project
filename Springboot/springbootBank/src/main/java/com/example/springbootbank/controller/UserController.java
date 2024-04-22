package com.example.springbootbank.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.User;
import com.example.springbootbank.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/User")
public class UserController {
    @Resource

    @Autowired
    private UserMapper userMapper;


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
}
