package com.example.springbootbank.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springbootbank.entity.AdminOnline;
import com.example.springbootbank.mapper.AdminOnlineMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    public List<AdminOnline> findAll(){

        QueryWrapper<AdminOnline> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("*");

        List<AdminOnline> list=adminOnlineMapper.selectList(queryWrapper);
        System.out.println(list);
        return list;
    }

}

