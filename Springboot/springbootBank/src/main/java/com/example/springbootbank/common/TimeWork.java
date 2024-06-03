
package com.example.springbootbank.common;
//定时任务中心

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.springbootbank.entity.*;
import com.example.springbootbank.entity.otherEntity.AbnormalMsg;
import com.example.springbootbank.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

public class TimeWork {
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
    LenderMapper lenderMapper;
    @Autowired
    ReLoansMapper reLoansMapper;

    @Autowired
    UserLoansMapper userLoansMapper;

    @Autowired
    UserCreditMapper userCreditMapper;
    //开始定时任务
    public void workstart(){

    }
    //每天计算每个用户购买每个理财产品的利润或者亏损（理财产品收益）
    public void productwork(){
        List<UserProduct> userProducts=userProductMapper.selectList(null);
        for(int i=0;i<userProducts.size();i++){
            //获取当前用户的理财产品购买记录
            List<UserProductDetails> details= JSONArray.parseArray(userProducts.get(i).getProduct(),UserProductDetails.class);
            int flag=0;//看该用户其下是否有产品的余额发生了变化,用于节省性能
            System.out.println(userProducts.get(i));
            for(int j=0;j<details.size();j++){//该用户购买的所有产品
                if(details.get(j).getState()==1){//该用户的该产品还未结束
                    flag=1;
                    Product product=productMapper.selectById(details.get(j).getProductid());
                    float balance=0;
                    if(product.getType()==2){//限期（七日年化）
                        balance=details.get(j).getBalance()*((float) ((100+product.getRate()/7)/100));
                    }
                    else {//固期，固期利率
                        float rate=(float) (100+(product.getRate()/(product.getMinday())))/100;
                        balance=(details.get(j).getBalance())*(rate);
                    }
                    details.get(j).setBalance(balance);
                }
            }
            if(flag==1){//更新该用户的理财产品情况
                userProducts.get(i).setProduct(JSONArray.toJSONString(details));
                System.out.println(userProducts.get(i));
                userProductMapper.updateById(userProducts.get(i));
            }
        }
    }
    //检查每个贷款设置逾期
    public void setloanspay(){
        QueryWrapper<UserLoans> queryWrapper=new QueryWrapper<>();
        List<UserLoans> userLoansList=userLoansMapper.selectList(queryWrapper.eq("ispass",2));//找到所有正在生效的贷款
        LocalDate nowtime=LocalDate.now();
        for(int i=0;i<userLoansList.size();i++){
            QueryWrapper<ReLoans> reLoansQueryWrapper=new QueryWrapper<>();
            List<ReLoans> reLoansList=reLoansMapper.selectList(reLoansQueryWrapper.eq("lid",userLoansList.get(i).getId()).orderByDesc("id"));
            //找到所有账单
            if(reLoansList.get(0).getState()==0&&nowtime.isAfter(reLoansList.get(0).getTime())&&nowtime.isBefore(reLoansList.get(0).getTime().plusMonths(1))){//逾期未还(一个月内)
                reLoansList.get(0).setState(2);
                reLoansMapper.updateById(reLoansList.get(0));//设置成功
                //设置违规记录
                UserCredit userCredit=userCreditMapper.selectOne(Wrappers.<UserCredit>lambdaQuery().eq(UserCredit::getUid,userLoansList.get(i).getUid()));
                userCredit.setDefaults(userCredit.getDefaults()+1);
                userCreditMapper.updateById(userCredit);
            }
            else if(reLoansList.get(0).getState()==2&&nowtime.isEqual(reLoansList.get(0).getTime().plusMonths(1))){//逾期超过一个月了
                ReLoans reLoans1=new ReLoans();
                reLoans1.setLid(reLoansList.get(0).getLid());
                reLoans1.setCid(reLoansList.get(0).getCid());
                reLoans1.setTime(nowtime);
                reLoans1.setUid(reLoansList.get(0).getUid());
                Lender lender=lenderMapper.selectById(userLoansList.get(i).getLid());
                float rate=lender.getRate()/1200;//先获取贷款利息（月）
                float cost=0;
                float benjin=userLoansList.get(i).getCost();//本金
                float needreturncost=userLoansList.get(i).getNeedreturncost();//还需归还本金
                float time=userLoansList.get(i).getTimelimit();//期限
                if(userLoansList.get(i).getReturntype()==1){//等额本息
                    cost=(float)( (benjin*rate*Math.pow(1+rate,time))/(Math.pow(1+rate,time)-1));
                }
                else if(userLoansList.get(i).getReturntype()==2){//等额本金
                    cost=(benjin/time)+needreturncost*rate;
                }
                reLoans1.setCost(reLoansList.get(0).getCost()+cost);//设置消费
                reLoans1.setState(0);
                reLoans1.setReturnday(reLoansList.get(0).getReturnday()+1);
                reLoansMapper.insert(reLoans1);
            }
        }

    }
    //月初创建上个月的信用卡账单（所有银行卡）
    public void everCreditCardbills(){
        List<CreditCard> creditCards=creditCardMapper.selectList(null);
        for(int i=0;i<creditCards.size();i++){
            if(creditCards.get(i)!=null){//存在该信用卡
                cbills(creditCards.get(i));
            }
        }
    }

    //月初创建上个月的信用卡账单（单张银行卡）
    public void cbills(CreditCard creditCard){
        Integer ccid=creditCard.getCid();
        LocalDateTime now=LocalDateTime.now();
        LocalDateTime localDatestart=LocalDateTime.of(now.minusMonths(1L).with(TemporalAdjusters.firstDayOfMonth()).toLocalDate(), LocalTime.MIN);
        LocalDateTime localDateslast=LocalDateTime.of(now.minusMonths(1L).with(TemporalAdjusters.lastDayOfMonth()).toLocalDate(),LocalTime.MAX);
        BankCard bankCard=bankCardMapper.selectById(ccid);
        if(bankCard!=null){//存在该银行卡
            List<Detail> details= JSONArray.parseArray(bankCard.getDetail(),Detail.class);
            Debt debt=new Debt();
            List<Long> pays=new ArrayList<>();
            float needreturn=0;
            for(int i=0;i<details.size();i++){
                //筛选出时间范围内的并且是消费和取出的记录
                if(localDateslast.isAfter(details.get(i).getPaytime())&&localDatestart.isBefore(details.get(i).getPaytime())){
                    if(details.get(i).getType().equals("消费")||details.get(i).getType().equals("取出")){
                        needreturn=needreturn+details.get(i).getCost();
                        pays.add(details.get(i).getId());
                    }
                }
            }
            //创建账单
            if(pays.size()>0){//有账单产生
                debt.setInterest(0);
                debt.setNeedreturn(Math.round(needreturn*100)/100f);
                debt.setDays(0);
                debt.setId(IdGeneratorSnowlake.getInstance().snowflakeId());
                debt.setCost(Math.round(needreturn*100)/100f);
                debt.setReturnmoney(0);
                //设置本月的1号到10号
                debt.setTime(localDatestart.plusMonths(1).toLocalDate());
                debt.setTimelast(localDatestart.plusMonths(1).plusDays(9).toLocalDate());
                //
                debt.setBills(pays);
                //添加账单
                List<Debt> Debts= JSONArray.parseArray(creditCard.getDebt(),Debt.class);
                Debts.add(debt);
                creditCard.setDebt(JSONArray.toJSONString(Debts));
                creditCardMapper.updateById(creditCard);
            }
        }
    }
    //每天检查是否存在信用卡逾期账单并设置逾期天数
    public void setoverdue(){
        List<CreditCard> creditCards=creditCardMapper.selectList(null);//获取所有的信用卡
        for(int i=0;i<creditCards.size();i++){
            Integer flag=0;
            List<Debt> Debts= JSONArray.parseArray(creditCards.get(i).getDebt(),Debt.class);
            for(int j=0;j<Debts.size();j++){
                if((Debts.get(j).getNeedreturn()+Debts.get(j).getInterest())>=0.01){//账单未还清
                    if(Debts.get(j).getDays()>0){//已经逾期的
                        flag=1;
                        Debts.get(j).setDays(Debts.get(j).getDays()+1);
                        float rates=Debts.get(j).getInterest()+(Debts.get(j).getNeedreturn()+Debts.get(j).getInterest())*((creditCards.get(i).getRate()));//每日利率
                        BigDecimal bigDecimal=new BigDecimal(rates);
                        float balance2=bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP).floatValue();
                        Debts.get(j).setInterest(balance2);
                    }
                    else{//尚未逾期的
                        LocalDate now=LocalDate.now();
                        if(Debts.get(j).getTimelast().isBefore(now)){//第一天逾期
                            flag=1;
                            Debts.get(j).setDays(1);
                            float overdue=Debts.get(j).getNeedreturn()*creditCards.get(i).getOverdue();//本金滞留金
                            float rates=Debts.get(j).getNeedreturn()*creditCards.get(i).getRate();//每日利率
                            BigDecimal bigDecimal=new BigDecimal(rates+overdue);
                            float balance2=bigDecimal.setScale(2,BigDecimal.ROUND_HALF_UP).floatValue();
                            Debts.get(j).setInterest(balance2);
                            BankCard bankCard=bankCardMapper.selectById(creditCards.get(i).getCid());
                            //
                            bankCard.setAbnormal(1);//设置异常
                            List<AbnormalMsg> abnormalMsgs=JSONArray.parseArray(bankCard.getAbnormalmsg(), AbnormalMsg.class);
                            AbnormalMsg abnormalMsg=new AbnormalMsg();
                            LocalDateTime nowtime=LocalDateTime.now();
                            abnormalMsg.setTime(nowtime);
                            abnormalMsg.setDescription("信用卡账单逾期未还");
                            abnormalMsg.setState(1);
                            abnormalMsg.setType(3);
                            abnormalMsgs.add(abnormalMsg);
                            bankCard.setAbnormalmsg(JSONArray.toJSONString(abnormalMsgs));
                            bankCardMapper.updateById(bankCard);
                            //
                            UserCredit userCredit=userCreditMapper.selectOne(Wrappers.<UserCredit>lambdaQuery().eq(UserCredit::getUid,bankCard.getUid()));
                            userCredit.setDefaults(userCredit.getDefaults()+1);
                            userCredit.setCredit(userCredit.getCredit()-10);
                            userCreditMapper.updateById(userCredit);
                        }
                    }
                }
            }
            if(flag==1){//有修改的
                creditCards.get(i).setDebt(JSONArray.toJSONString(Debts));
                creditCardMapper.updateById(creditCards.get(i));
                System.out.println(Debts);
            }
        }
    }


}
