

package com.example.springbootbank.controller;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootbank.common.IdGeneratorSnowlake;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.*;
import com.example.springbootbank.mapper.BankCardMapper;
import com.example.springbootbank.mapper.EnterpriseMapper;
import com.example.springbootbank.mapper.ProductMapper;
import com.example.springbootbank.mapper.UserProductMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/Product")
public class ProductController {
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
    @PostMapping("/getProduct")//获取所有商品(上架的产品)
    public Result getProduct(){
        QueryWrapper<Product> queryWrapper=new QueryWrapper<>();
        List<Product> products=productMapper.selectList(queryWrapper.eq("isdeleted",1));
        if(products.size()>0){
            return Result.success(products);
        }
        return Result.error("300","搜索不到任何已上架的商品！");
    }
    @PostMapping("/getProductremove")//获取所有商品(下架的产品)
    public Result getProductremove(){
        QueryWrapper<Product> queryWrapper=new QueryWrapper<>();
        List<Product> products=productMapper.selectList(queryWrapper.eq("isdeleted",0));
        if(products.size()>0){
            return Result.success(products);
        }
        return Result.error("300","搜索不到任何已下架的商品！");
    }
    @PostMapping("/upProduct")//商品上架
    public Result upProduct(@RequestBody Product product){
        //先查找是否有用户购买该产品并且还在生效当中
        product.setIsdeleted(1);
        if(productMapper.updateById(product)==1){
            return Result.success();
        }
        return Result.error("300","产品上架失败！");
    }
    @PostMapping("/removeProduct")//商品下架
    public Result removeProduct(@RequestBody Product product){
        //先查找是否有用户购买该产品并且还在生效当中
        List<UserProduct> userProducts=userProductMapper.selectList(null);
        for(int i=0;i<userProducts.size();i++){
            //获取当前用户的理财产品购买记录
            List<UserProductDetails> details= JSONArray.parseArray(userProducts.get(i).getProduct(),UserProductDetails.class);
            for(int j=0;j<details.size();j++){
                if((details.get(j).getProductid()==product.getId())&&details.get(j).getState()==1){
                    return Result.error("500","该产品有用户购买并在生效阶段，暂时无法下架！");
                }
            }
        }
        product.setIsdeleted(0);
        if(productMapper.updateById(product)==1){
            return Result.success();
        }
        return Result.error("300","产品下架失败！");
    }
    @PostMapping("/getProductOne")//获取某个商品
    public Result getProduct(@RequestBody Map map){
        Integer id=(Integer)map.get("id");
        Product product=productMapper.selectById(id);
        if(product!=null){
            return Result.success(product);
        }
        return Result.error("300","搜索不到任何商品！");
    }
    @PostMapping("/addProduct")//添加商品
    public Result addProduct(@RequestBody Product product){
        System.out.println(product);
        List<Rates> rates= JSONArray.parseArray(product.getHistoricalrate(),Rates.class);
        LocalDate now=LocalDate.now();
        Rates rate=new Rates();
        rate.setRate(product.getRate());
        rate.setTime(now);
        rates.add(rate);
        product.setHistoricalrate(JSONArray.toJSONString(rates));
        LocalDateTime localDateTime=LocalDateTime.now();
        product.setCreatetime(localDateTime);
        if(productMapper.insert(product)==1){
            return Result.success();
        }
        return Result.error("500","添加商品失败！");
    }
    @PostMapping("/editProduct")//编辑更新商品(不修改利率)
    public Result editProduct(@RequestBody Product product){
        if(productMapper.updateById(product)==1){
            return Result.success();
        }
        return Result.error("500","更新商品失败！");
    }
    @PostMapping("/selectpages")//搜索产品，分页
    public Result selectpages(@RequestBody Map map){
        Integer type=(Integer) map.get("type");
        String name=(String) map.get("name");
        Integer pageNum=(Integer) map.get("pageNum");
        Integer pageSize=(Integer) map.get("pageSize") ;
        Page<Product> page= Page.of(pageNum,pageSize);
        LambdaQueryWrapper<Product> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(Product::getName,name).eq(Product::getIsdeleted,1);
        if(type!=0){
            queryWrapper.eq(Product::getType,type);
        }
        productMapper.selectPage(page,queryWrapper);
        return Result.success(page);
    }
    @PostMapping("/RateChange")//修改利率
    public Result RateChange(@RequestBody Map map){
        Integer pid=(Integer) map.get("pid");
        String rate1=(String) map.get("rate");
        double rate=Double.valueOf(rate1);
        System.out.println(rate);
        Product product=productMapper.selectById(pid);
        product.setRate(rate);
        List<Rates> rates= JSONArray.parseArray(product.getHistoricalrate(),Rates.class);
        LocalDate now=LocalDate.now();
        int flag=0;
        for(int i=0;i<rates.size();i++){
            if(now.isEqual(rates.get(i).getTime())){//修改今日利率
                flag=1;
                rates.get(i).setRate(rate);
                break;
            }
        }
        if(flag==0){//新添加的今日利率
            Rates rateOne=new Rates();
            rateOne.setTime(now);
            rateOne.setRate(rate);
            rates.add(rateOne);
        }
        product.setHistoricalrate(JSONArray.toJSONString(rates));
        if(productMapper.updateById(product)==1){
            return Result.success();
        }
        return Result.error("500","利率修改失败！");
    }
    @PostMapping("/BuyProduct")//购买某个商品
    public Result BuyProduct(@RequestBody Map map){
        Integer productId=(Integer) map.get("productid");
        Product product=productMapper.selectById(productId);
        if(product.getIsdeleted()!=0){//产品未下架
            if(product.getAmount()>product.getSum()){//产品尚未售罄
                Integer cardid=(Integer) map.get("cardid");
                float cost=Float.valueOf((String)map.get("cost"));
                BankCard bankCard=bankCardMapper.selectById(cardid);
                if(bankCard.getBalance()>=cost){//余额足够
                    Integer uid=(Integer) map.get("uid");
                    UserProduct userProduct=userproductMapper.selectOne(Wrappers.<UserProduct>lambdaQuery().eq(UserProduct::getUid,uid));
                    //设置细节
                    UserProductDetails userProductDetails=new UserProductDetails();
                    userProductDetails.setCost(cost);
                    userProductDetails.setBalance(cost);
                    userProductDetails.setId(IdGeneratorSnowlake.getInstance().snowflakeId());
                    DateTimeFormatter df=DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    LocalDateTime now=LocalDateTime.now();
                    String time=df.format(now);
                    now=LocalDateTime.parse(time,df);
                    userProductDetails.setPaytime(now);
                    userProductDetails.setCardid(cardid);
                    userProductDetails.setProductid(productId);
                    userProductDetails.setState(1);
                    //
                    if(userProduct!=null){//已经存在了一条数据
                        List<UserProductDetails> details= JSONArray.parseArray(userProduct.getProduct(),UserProductDetails.class);
                        details.add(userProductDetails);
                        userProduct.setProduct(JSONArray.toJSONString(details));
                        if(buyproductandsetcard(uid,userProductDetails)==1){
                            userproductMapper.updateById(userProduct);
                            return Result.success();
                        }
                        return Result.error("500","账户出现问题，购买失败，请联系工作人员或到线下购买");
                    }
                    else{
                        userProduct=new UserProduct();
                        userProduct.setUid(uid);
                        Enterprise enterprise=enterpriseMapper.selectOne(Wrappers.<Enterprise>lambdaQuery().eq(Enterprise::getUid,uid));
                        if(enterprise!=null){
                            userProduct.setEnterprise(1);
                        }
                        else{
                            userProduct.setEnterprise(2);
                        }
                        List<UserProductDetails> details=new ArrayList<>();
                        details.add(userProductDetails);
                        userProduct.setProduct(JSONArray.toJSONString(details));
                        if(buyproductandsetcard(uid,userProductDetails)==1){
                            userproductMapper.insert(userProduct);
                            return Result.success();
                        }
                        return Result.error("500","账户出现问题，购买失败，请联系工作人员或到线下购买");
                    }
                }
                return Result.error("500","当前账户余额不足，请更换账户进行购买");
            }
            return Result.error("500","抱歉，当前商品已售罄！！");
        }
        return Result.error("500","抱歉，该商品已下架，请刷新查看最新数据！");
    }

    //购买商品操作(设置银行卡信息和账单信息)
    public int buyproductandsetcard(int uid, UserProductDetails userProductDetails){
        BankCard bankCard=bankCardMapper.selectById(userProductDetails.getCardid());
        if(bankCard!=null){
            List<Detail> details= JSONArray.parseArray(bankCard.getDetail(),Detail.class);
            bankCard.setBalance(bankCard.getBalance()-userProductDetails.getCost());//设置当前余额
            Detail detail=new Detail();
            detail.setId(userProductDetails.getId());
            detail.setPaytime(userProductDetails.getPaytime());
            detail.setCost(userProductDetails.getCost());
            detail.setPlace("网上银行");
            detail.setType("理财");
            String describe="用于购买理财产品，产品名称：";
            Product product=productMapper.selectById(userProductDetails.getProductid());
            product.setSum(product.getSum()+ userProductDetails.getCost());
            describe=describe+product.getName();
            detail.setDescribe(describe);
            details.add(detail);
            bankCard.setDetail(JSONArray.toJSONString(details));
            //产品处理
            productMapper.updateById(product);
            bankCardMapper.updateById(bankCard);
            return 1;
        }
        return 0;
    }

}
@Data
class Rates{
    public double rate;
    public LocalDate time;
}
