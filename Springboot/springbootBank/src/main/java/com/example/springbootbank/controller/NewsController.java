package com.example.springbootbank.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.News;
import com.example.springbootbank.mapper.NewsMapper;
import com.example.springbootbank.service.INewsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/news")
public class NewsController {
    @Resource
    private INewsService newsService;

    @Resource
    private NewsMapper newsMapper;

//    private Result returnObj=null;

    @PostMapping("/add")//添加新闻（未上架）
    public Result save(@RequestBody News news){
        news.setTime(DateUtil.now());
//        boolean flag=false;
//        Integer flag=0;
//        if(id==null){
//            flag=newsMapper.insert(news);
//        }else{
//            flag=newsMapper.updateById(news);
//        }
        boolean flag=newsService.saveOrUpdate(news);

        if(flag==true){
            return Result.success();
        }
        return Result.error();
//        returnObj=new Result("",newsService.saveOrUpdate(news),"200");
//        return returnObj;
    }

    @DeleteMapping("/{id}")//删除新闻(真正删除)
    public Result delete(@PathVariable Integer id){
        boolean flag = newsService.removeById(id);
        if(flag==true){
            return Result.success();
        }
        return Result.error();
//        return returnObj;
    }
    @PostMapping("/downnews")//单个下架
    public Result updateByBatch(@RequestBody Integer id){
        News news=newsMapper.selectById(id);
        news.setNewrelease(0);
        if(newsMapper.updateById(news)==1){
            return Result.success();
        }
        return Result.error("500","下架失败");
    }
    @PostMapping("/deleteByBatch")//批量删除
    public Result deleteByIds(@RequestBody List<Integer> ids){
        if(newsMapper.deleteBatchIds(ids)==1){
            return Result.success();
        }
        return Result.error();
    }

    @PostMapping("/donwByBatch")//批量下架
    public Result donwByBatch(@RequestBody List<Integer> ids){
        Integer flag=0;
        for(int i=0;i<ids.size();i++){
            News news=newsMapper.selectById(ids.get(i));
            news.setNewrelease(0);
            if(newsMapper.updateById(news)!=1){
                flag=1;
                break;
            }
        }
        if(flag==0){
            return Result.success();
        }
        return Result.error();
    }
    @PostMapping("/updateByBatch")//批量发布
    public Result updateByBatch(@RequestBody List<Integer> ids){
        Integer flag=0;
        LocalDateTime now=LocalDateTime.now();
        for(int i=0;i<ids.size();i++){
            News news=newsMapper.selectById(ids.get(i));
            news.setNewrelease(1);
            news.setUptime(now.toString());//设置发布时间
            if(newsMapper.updateById(news)!=1){
                flag=1;
                break;
            }
        }
        if(flag==0){
            return Result.success();
        }
        return Result.error();
    }
    @PostMapping("/select")//app端加载新闻
    public Result select(@RequestBody Map map ){
        Integer pageNum=(Integer) map.get("pageNum") ;
        String type=(String)map.get("type");
        LambdaQueryWrapper<News> wrapper = Wrappers.lambdaQuery();
        wrapper.select(News::getCategory,News::getNewrelease,News::getId,News::getImg,News::getTime,News::getTitle);
        if(!type.equals("")&&type!=null){
            wrapper.eq(News::getCategory,type);
        }
        Page<News> userPage = newsMapper.selectPage(new Page<News>(pageNum, 5), wrapper.eq(News::getNewrelease,1).orderByDesc(News::getId));
        return Result.success(userPage);
    }

    @PostMapping("/updateone")//发布一个新闻
    public Result updateone(@RequestBody Map map ){
        Integer id=(Integer) map.get("id") ;
        News news=newsMapper.selectById(id);
        LocalDateTime now=LocalDateTime.now();
        news.setUptime(now.toString());
        news.setNewrelease(1);
        newsMapper.updateById(news);
        return Result.success();
    }
    @PostMapping("/selectbyid")//根据id搜索新闻
    public Result selectbyid(@RequestBody Map map ){
        Integer id=(Integer) map.get("id") ;
        News news=newsMapper.selectById(id);
        news.setReadCount(news.getReadCount()+1);
        newsMapper.updateById(news);//阅读次数加1
        return Result.success(news);
    }
    @PostMapping("/gettotal")//获取某种类型的新闻总数
    public Result gettotal(@RequestBody Map map ){
        String type=(String) map.get("type") ;
        QueryWrapper<News> queryWrapper=new QueryWrapper<>();
        if(!type.equals("")&&type!=null){//获取类型
            queryWrapper.eq("category",type);
        }
        List<News> news=newsMapper.selectList(queryWrapper.eq("newrelease",1));
        return Result.success(news.size());
    }
    @GetMapping("/search")//搜索新闻(分页)
    public Result findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search,
                           @RequestParam(defaultValue = "0") Integer type
                              ) {
        LambdaQueryWrapper<News> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(News::getNewrelease,type);
        if (StrUtil.isNotBlank(search)) {
            wrapper.like(News::getTitle, search);
        }
        Page<News> userPage = newsMapper.selectPage(new Page<>(pageNum, pageSize), wrapper.orderByDesc(News::getId));
        return Result.success(userPage);
//        returnObj=new ReturnObj("",userPage,"200");
//        return returnObj;
    }
}
