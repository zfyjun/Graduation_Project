package com.example.springbootbank.service;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.springbootbank.entity.AdminOnline;
import com.example.springbootbank.mapper.AdminOnlineMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AdminOnlineService {

    @Resource
    private AdminOnlineMapper adminOnlineMapper;

    public void setOnline(String username) {
        UpdateWrapper<AdminOnline> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("name", username).set("online", 1);
        adminOnlineMapper.update(null, updateWrapper);
    }

    public void setOffline(String username) {
        UpdateWrapper<AdminOnline> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("name", username).set("online", 0);
        adminOnlineMapper.update(null, updateWrapper);
    }

    // 你还可以添加其他需要的方法
}