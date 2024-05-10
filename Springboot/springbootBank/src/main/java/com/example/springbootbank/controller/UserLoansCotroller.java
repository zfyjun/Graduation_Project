package com.example.springbootbank.controller;

import com.example.springbootbank.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/UserLoans")
public class UserLoansCotroller {
    @Resource

    @Autowired
    BankCardMapper bankCardMapper;

    @Autowired
    ProductMapper productMapper;
    @Autowired
    UserProductMapper userproductMapper;

    @Autowired
    EnterpriseMapper enterpriseMapper;

    @Autowired
    UserProductMapper userProductMapper;

    @Autowired
    UserLoansMapper userLoansMapper;

    @Autowired
    LenderMapper lenderMapper;


}
