package com.example.springbootbank.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springbootbank.entity.AdminOnline;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminOnlineMapper extends BaseMapper<AdminOnline> {
//    List<AdminOnline> selectList(QueryWrapper<AdminOnline> queryWrapper);
}
