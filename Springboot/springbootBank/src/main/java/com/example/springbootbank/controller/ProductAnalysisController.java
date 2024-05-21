package com.example.springbootbank.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.*;
import com.example.springbootbank.mapper.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/productanalysis")
public class ProductAnalysisController {
    @Resource
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CodeMapper codeMapper;

    @Autowired
    private BankCardMapper bankCardMapper;
    @Autowired
    private ProductMapper productMapper;


    @Autowired
    UserProductMapper userProductMapper;

    @Autowired
    CreditCardMapper creditCardMapper;
    @Autowired
    MarketMapper marketMapper;
    @Autowired
    UserLoansMapper userLoansMapper;

    @Autowired
    LenderMapper lenderMapper;
    @Autowired
    ReLoansMapper reLoansMapper;
    @Autowired
    MarketNameMapper marketNameMapper;
    @Autowired
    UserCreditMapper userCreditMapper;

    @Autowired
    EnterpriseMapper enterpriseMapper;
    @Autowired
    UserInfoMapper userInfoMapper;
    @Autowired
    MarketCopyMapper marketCopyMapper;
    @Autowired
    ProductClassMapper productClassMapper;

    @PostMapping("/analysis")//分析产品(总体)
    public Result getCardsDetail2(){
        QueryWrapper<Product> productQueryWrapper=new QueryWrapper<>();
        List<Product> productList=productMapper.selectList(productQueryWrapper.eq("isdeleted",1));
        List<Productanalysis> list = new ArrayList<>();
        List<UserProduct > userProductList=userProductMapper.selectList(null);
        for(int i=0;i<productList.size();i++){
            Productanalysis productanalysis=new Productanalysis();
            productanalysis.setProduct(productList.get(i));
            //开始查找本产品购买人数
            Integer buyp=0,buytwice=0;
            for(int j=0;j<userProductList.size();j++){
                List<UserProductDetails> userProductDetails=JSONArray.parseArray(userProductList.get(j).getProduct(),UserProductDetails.class);
                Integer flag=0;
                for(int n=0;n<userProductDetails.size();n++){
                    if(userProductDetails.get(n).getProductid()==productList.get(i).getId()){//找到了购买本产品的用户
                        if(flag==0){
                            buyp++;
                        }
                        if(flag==1){//该用户已经购买了一次，这次是第二次进入了
                            buytwice++;
                            break;
                        }
                        flag=1;
                    }
                }
            }
            productanalysis.setBuyp(buyp);
            productanalysis.setBuytwice(buytwice);
            list.add(productanalysis);
        }
        if(list.size()>0){
            return Result.success(list);
        }
        return Result.error("500","分析失败");
    }
    @PostMapping("/editrisk")//产品风险等级修改
    public Result editrisk(@RequestBody Map map){
        Integer pid=(Integer) map.get("pid");
        Integer risk=(Integer) map.get("risk");
        Product product=productMapper.selectById(pid);
        product.setRisk(risk);
        productMapper.updateById(product);
        return Result.success();
    }
    @PostMapping("/addnewclass")//新建分类
    public Result addclass(@RequestBody ProductClass productClass){
        productClassMapper.insert(productClass);
        return Result.success();
    }

    @PostMapping("/getnames")//获取名字
    public Result getnames(@RequestBody Map map){
        Integer cid=(Integer)map.get("classid") ;
        List<String> list=new ArrayList<>();
        ProductClass productClass=productClassMapper.selectById(cid);
        List<Integer> pids=JSONArray.parseArray(productClass.getPids(),Integer.class);
        for(int i=0;i<pids.size();i++){
            Product product=productMapper.selectById(pids.get(i));
            list.add(product.getName());
        }
        return Result.success(list);
    }

    @PostMapping("/getclasses")//直接获取分类
    public Result getclasses(){
        List<ProductClass> productClassList=productClassMapper.selectList(null);
        return Result.success(productClassList);
    }
    @PostMapping("/getclassesanalysis")//分类分析
    public Result getclassesanalysis(){

        return Result.success();
    }

    @PostMapping("/productclass")//产品分类
    public Result productclass(){
        List<ProductClass> productClassList=productClassMapper.selectList(null);
        QueryWrapper<Product> queryWrapper=new QueryWrapper<>();
        List<Product> productList=productMapper.selectList(queryWrapper.eq("isdeleted",1));
        for(int i=0;i<productClassList.size();i++){
            regulation regulationOne= JSONObject.parseObject(productClassList.get(i).getRegulation(),regulation.class);
            List<Integer> pids=new ArrayList<>();
            for(int j=0;j<productList.size();j++ ){
                if(regulationOne.getPrice()==setprice(productList.get(j))&&regulationOne.getRisk()==setrisk(productList.get(j))){
                    if(regulationOne.getType()==0&&regulationOne.getSum()!=2){//只不设置类型
                        if(regulationOne.getSum()==setsum(productList.get(j))){
                            pids.add(productList.get(j).getId());
                        }
                    }
                    else if(regulationOne.getType()!=0&&regulationOne.getSum()==2){//只不设置总额
                        if(regulationOne.getType()==productList.get(j).getType()){
                            pids.add(productList.get(j).getId());
                        }
                    }
                    else if(regulationOne.getType()==0&&regulationOne.getSum()==2){//都不设置
                        pids.add(productList.get(j).getId());
                    }
                }
            }
            productClassList.get(i).setRegulation(JSONObject.toJSONString(regulationOne));
            productClassList.get(i).setPids(JSONArray.toJSONString(pids));
            productClassMapper.updateById(productClassList.get(i));
        }
        return Result.success(productClassList);
    }
    @PostMapping("/analysisrisk")//分析单个产品的风险等级
    public Result analysisrisk(@RequestBody Map map){
        Integer pid=(Integer) map.get("pid");
        Integer risk=3;//基础等级为3
        Integer add=0;
        Integer low=0;
        Product product=productMapper.selectById(pid);
        List<Rates> ratesList= JSONArray.parseArray(product.getHistoricalrate(),Rates.class);
        for(int i=0;i<ratesList.size();i++){
            if(ratesList.get(i).getRate()>0){
                add++;
            }
            else if(ratesList.get(i).getRate()<0){
                low++;
            }
        }
        if(add-low>4){
            risk=1;
        }
        else if(add-low>0&&add-low<=4){
            risk=2;
        }
        else if(add-low==0){
            risk=3;
        }
        else if(add-low<0&&add-low>=-4){
            risk=4;
        }
        else if(add-low<-4){
            risk=5;
        }
        return Result.success(risk);
    }

    public Integer  setprice(Product product){//设置价格等级
        if(product.getPrice()>0.1&&product.getPrice()<=50){//低价格
            return 0;
        }
        else if(product.getPrice()>50&&product.getPrice()<=1000){//中等价格
            return 1;
        }
        else if(product.getPrice()>1000){//高起购价
            return 2;
        }
        return 0;
    }
    public Integer  setrisk(Product product){//设置风险
        if(product.getRisk()<3){//低风险
            return 0;
        }
        else if(product.getRisk()==3){//中等价格
            return 1;
        }
        else if(product.getRisk()>3){//高起购价
            return 2;
        }
        return 0;
    }
    public Integer setsum(Product product){//设置额度
        if(product.getAmount()<=1000000){
            return 0;
        }
        else if(product.getAmount()>1000000){
            return 1;
        }
        return 0;
    }
}
@Data
class Productanalysis{
    Product product;
    Integer buyp;//购买人数
    Integer buytwice;//回头客人数
}
@Data
class regulation{//分类规则
    Integer price;//0为低起购价，1为中起购价，2为高起购价格
    Integer risk;//0为低风险，1为中风险，2为高风险
    Integer type;//1为固期，2为限期,0不区分
    Integer sum;//0为小额度，1为大额度,2不区分
}
@Data
class AnalysisClass{//分类分析
    ProductClass productClass;
    Integer buyp;//购买人数
    float sum;//购买总金额
}