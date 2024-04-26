package com.example.springbootbank.common;

import com.example.springbootbank.entity.UserProduct;
import com.example.springbootbank.mapper.BankCardMapper;
import com.example.springbootbank.mapper.ProductMapper;
import com.example.springbootbank.mapper.UserMapper;
import com.example.springbootbank.mapper.UserProductMapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.List;

//用户产品利率收益计算中心（定时任务）
public class ProductWork {
    @Resource

    @Autowired
    BankCardMapper bankCardMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    ProductMapper productMapper;
    @Autowired
    UserProductMapper userProductMapper;

    public void ProductUserWork(){
        List<UserProduct> userProducts=userProductMapper.selectList(null);
        for(int i=0;i<userProducts.size();i++){
            System.out.println(userProducts.get(i));
        }
    }
}
