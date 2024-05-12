package com.example.springbootbank.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springbootbank.entity.UserInfo;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoMapper extends BaseMapper<UserInfo> {
}
