package com.example.springbootbank.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import java.util.Comparator;
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
        regulation regulationnew=JSONObject.parseObject(productClass.getRegulation(),regulation.class);
        List<ProductClass> productClassList=productClassMapper.selectList(null);
        for(int i=0;i<productClassList.size();i++){
           if( productClass.getName().equals(productClassList.get(i).getName()) ){
               return Result.error("500","分类名称已存在，请更换分类名称");
           }
           else {
               regulation regulationold=JSONObject.parseObject(productClassList.get(i).getRegulation(),regulation.class);
               if((regulationold.getType()==regulationnew.getType())&&(regulationold.getSum()==regulationnew.getSum())&&(regulationold.getRisk()==regulationnew.getRisk())&&(regulationold.getPrice()==regulationnew.getPrice())){
                   return Result.error("500","存在相同规则的分类，请更改规则");
               }
           }
        }
        productClassMapper.insert(productClass);
        return Result.success();
    }
    @PostMapping("/deleteclass")//删除分类
    public Result deleteclass(@RequestBody Map map){
        Integer cid=(Integer) map.get("id");
        productClassMapper.deleteById(cid);
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
        QueryWrapper<ProductClass> queryWrapper=new QueryWrapper<>();
        List<ProductClass> productClassList=productClassMapper.selectList(queryWrapper.ne("pids","[]"));//排除没有产品的分类
        List<AnalysisClass> list=new ArrayList<>();
        for(int i=0;i<productClassList.size();i++){
            AnalysisClass analysisClass=new AnalysisClass();
            analysisClass.setProductClass(productClassList.get(i));
            analysisClass.setBuyp(0);
            analysisClass.setSum(0);
            List<Integer> ids=JSONArray.parseArray(productClassList.get(i).getPids(),Integer.class);
            for(int j=0;j<ids.size();j++){
                analysisClass.setBuyp(analysisClass.buyp+psum(ids.get(j)));
                Product product=productMapper.selectById(ids.get(j));
                analysisClass.setSum(analysisClass.getSum()+product.getSum());
            }
            list.add(analysisClass);
        }
        return Result.success(list);
    }
    @PostMapping("/persontuijian")//个人产品推荐
    public Result persontuijian(@RequestBody Map map){
        Integer id=(Integer) map.get("id");
        UserProduct userProduct=userProductMapper.selectOne(Wrappers.<UserProduct>lambdaQuery().eq(UserProduct::getUid,id));
        List<Product> tuijianproduct=new ArrayList<>();
        if(userProduct!=null){//有购买产品的历史记录
            List<UserProductDetails> userProductDetailsList=JSONArray.parseArray(userProduct.getProduct(),UserProductDetails.class);
            //记录用户购买每个产品的信息
            List<Productsort> productsorts=new ArrayList<>();
            //先设置第一个类型
            Productsort productsort1=new Productsort();
            productsort1.setPid(userProductDetailsList.get(0).getProductid());
            productsort1.setCost(userProductDetailsList.get(0).getCost());
            productsort1.setNumber(1);
            productsort1.setName(productMapper.selectById(userProductDetailsList.get(0).getProductid()).getName());
            productsorts.add(productsort1);
            //设置后面的类型
            for(int i=1;i<userProductDetailsList.size();i++){
                Integer flag=0;
                for(int j=0;j<productsorts.size();j++){
                    if(productsorts.get(j).getPid()==userProductDetailsList.get(i).getProductid()){//已经存在分类
                        flag=1;
                        productsorts.get(j).setCost(productsorts.get(j).getCost()+userProductDetailsList.get(i).getCost());
                        productsorts.get(j).setNumber(productsorts.get(j).getNumber()+1);
                        break;
                    }
                }
                if(flag==0){//需要新建
                    Productsort productsort=new Productsort();
                    productsort.setNumber(1);
                    productsort.setPid(userProductDetailsList.get(i).getProductid());
                    productsort.setCost(userProductDetailsList.get(i).getCost());
                    productsort.setName(productMapper.selectById(userProductDetailsList.get(i).getProductid()).getName());
                    productsorts.add(productsort);
                }
            }
            //排序(默认按花费从大到小)
            for(int i=0;i<productsorts.size();i++){
                for(int j=i+1;j<productsorts.size();j++){
                    if(productsorts.get(i).getCost()<productsorts.get(j).getCost()){
                        Productsort userProductDetails=productsorts.get(i);
                        productsorts.set(i,productsorts.get(j));
                        productsorts.set(j,userProductDetails);
                    }
                }
            }
            //现在开始根据用户购买记录来推荐类型
            List<ProductClass> productClassList=productClassMapper.selectList(null);
            for(int n=0;n<productsorts.size();n++){
                for(int i=0;i<productClassList.size();i++){
                    List<Integer> ids=JSONArray.parseArray(productClassList.get(i).getPids(),Integer.class);
                    Integer flag=0;
                    for(int j=0;j<ids.size();j++){
                        if(ids.get(j)==productsorts.get(n).getPid()){
                            flag=1;
                            break;
                        }
                    }
                    if(flag==1){//加入推荐
                        for(int j=0;j<ids.size();j++){
                            if(ids.get(j)!=productsorts.get(n).getPid()){
                                Product product=productMapper.selectById(ids.get(j));
                                if(product.getIsdeleted()==1){//没有下架的产品
                                    tuijianproduct.add(product);
                                }
                            }
                        }
                    }
                }
            }
            Integer sum=tuijianproduct.size();
            if(sum<5){
                for(int i=0;i<productsorts.size();i++){
                    tuijianproduct.add(productMapper.selectById(productsorts.get(i).getPid()));
                    sum++;
                    if(sum==5){
                        break;
                    }
                }
            }
        }
        else {//没有购买过理财产品
            QueryWrapper<Product> queryWrapper=new QueryWrapper<>();
            List<Product> selectProductlist=productMapper.selectList(queryWrapper.eq("isdeleted",1).orderByDesc("sum"));
            Integer sum=0;
            for(int i=0;i<selectProductlist.size();i++){
                tuijianproduct.add(selectProductlist.get(i));
                sum++;
                if(sum==5){
                    break;
                }
            }
        }
        return Result.success(tuijianproduct);
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

    public Integer psum(Integer pid){//获取某个商品的总购买人数
        Integer sum=0;
        List<UserProduct> userProductList=userProductMapper.selectList(null);
        for(int i=0;i<userProductList.size();i++){
            List<UserProductDetails> details=JSONArray.parseArray(userProductList.get(i).getProduct(),UserProductDetails.class);
            for(int j=0;j<details.size();j++){
                if(details.get(j).getProductid()==pid){
                    sum++;
                    break;
                }
            }
        }
        return sum;
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
@Data
class Productsort{
    String name;
    Integer pid;
    float cost;
    Integer number;
}