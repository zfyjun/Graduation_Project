package com.example.springbootbank.controller;


import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springbootbank.entity.AdminOnline;
import com.example.springbootbank.mapper.AdminOnlineMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author baomidou
 * @since 2023-01-16
 */
@RestController
@RequestMapping("/AdminOnline")
public class AdminOnlineController {


    @Resource
    private AdminOnlineMapper adminOnlineMapper;

    @PostMapping("/all")
    public JSONArray findAll(){

        QueryWrapper<AdminOnline> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("*");

        List<AdminOnline> list=adminOnlineMapper.selectList(queryWrapper);
        System.out.println(list);
        JSONArray array = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            JSONObject userObject= new JSONObject();
            userObject.set("username", list.get(i).getName());
            userObject.set("isOnline", list.get(i).getOnline());
            userObject.set("role",list.get(i).getRole());

            array.add(userObject);
        }
        return array;
    }

}

