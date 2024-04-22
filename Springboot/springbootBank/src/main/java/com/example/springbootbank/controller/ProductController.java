package com.example.springbootbank.controller;

import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.Product;
import com.example.springbootbank.mapper.BankCardMapper;
import com.example.springbootbank.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/Product")
public class ProductController {
    @Resource

    @Autowired
    BankCardMapper bankCardMapper;

    @Autowired
    ProductMapper productMapper;

    @PostMapping("/getProduct")//获取所有商品
    public Result getProduct(){
        List<Product> products=productMapper.selectList(null);
        if(products.size()>0){
            return Result.success(products);
        }
        return Result.error("300","搜索不到任何商品！");
    }
}
