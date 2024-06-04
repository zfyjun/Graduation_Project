package com.example.springbootbank.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.springbootbank.common.ReturnObj;
import com.example.springbootbank.entity.ChatHistory;
import com.example.springbootbank.mapper.ChatHistoryMapper;
import com.example.springbootbank.utils.SnowflakeIdWorker;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    private ReturnObj returnObj;

    @Resource
    private ChatHistoryMapper chatHistoryMapper;

    @PostMapping("/isRead")
    public ReturnObj isRead(@RequestBody Map map){

        String fromUser= (String) map.get("fromUser");
        String toUser= (String) map.get("toUser");
//        String name="root";
//        QueryWrapper<ChatHistory> queryWrapper=new QueryWrapper<>();
//        queryWrapper.eq("from_user",name);
//        queryWrapper.eq("un_read",0);
//        List<ChatHistory> list=chatHistoryMapper.selectList(queryWrapper);
//        System.out.println(list);


        // 使用 UpdateWrapper 来更新查询出的记录
        UpdateWrapper<ChatHistory> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("from_user", fromUser).eq("to_user",toUser).eq("un_read",0).set("un_read", 1);
        chatHistoryMapper.update(null, updateWrapper);

        returnObj=new ReturnObj("",null,"200");
        return returnObj;
    }

    @PostMapping("/unRead")
    public ReturnObj unRead(@RequestBody Map map){
        String name= (String) map.get("name");
        QueryWrapper<ChatHistory> queryWrapper=new QueryWrapper<>();

//        根据当前用户名字（to_user）查询所有为0（未读）的消息，按照发送的人（from_user）进行分组
        queryWrapper.select("from_user","count(*) as un_read")
                .eq("to_user",name)
                .eq("un_read",0)
                .groupBy("from_user");

        List<Map<String,Object>> list1=chatHistoryMapper.selectMaps(queryWrapper);
        System.out.println(list1);


        returnObj=new ReturnObj("",list1,"200");
        return returnObj;
    }

    @PostMapping("/add")
    public ReturnObj add(@RequestBody Map map){
        Map chatMap = (Map) map.get("message");
        System.out.println("message:"+map.get("message"));
        ChatHistory chatHistory=new ChatHistory();
        chatHistory.setFromUser(String.valueOf(chatMap.get("from")));
        chatHistory.setToUser(String.valueOf(chatMap.get("to")));
        chatHistory.setText(String.valueOf(chatMap.get("text")));

        SnowflakeIdWorker snowflakeIdWorker=new SnowflakeIdWorker(1,1);
        chatHistory.setId(String.valueOf(snowflakeIdWorker.nextId()));
//        System.out.println(chatHistory);

        returnObj=new ReturnObj("",chatHistoryMapper.insert(chatHistory),"200");
        return returnObj;
    }

    @PostMapping("/findAll")
    public ReturnObj findAll(@RequestBody Map map){

        String fromUser= String.valueOf(map.get("fromUser"));
        String toUser= String.valueOf(map.get("toUser"));

        QueryWrapper<ChatHistory> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("from_user",fromUser);
        queryWrapper.eq("to_user",toUser);
        queryWrapper.orderByAsc("create_time");

        List<ChatHistory> list1=chatHistoryMapper.selectList(queryWrapper);


        QueryWrapper<ChatHistory> queryWrapper1=new QueryWrapper<>();
        queryWrapper1.eq("from_user",toUser);
        queryWrapper1.eq("to_user",fromUser);
//        queryWrapper1.orderByAsc("create_time");

        List<ChatHistory> list2=chatHistoryMapper.selectList(queryWrapper1);

        List<ChatHistory> list3=new ArrayList<>();
        for (int i = 0; i < list2.size(); i++) {
            list3.add(list2.get(i));
        }
        for (int i = 0; i < list1.size(); i++) {
            list3.add(list1.get(i));
        }

        /*LocalDateTime min=list3.get(0).getCreateTime();*/
        for(int i=0;i<list3.size();i++){
            for(int j=i+1;j<list3.size();j++){
                if(list3.get(i).getCreateTime().isAfter(list3.get(j).getCreateTime())){
                    Collections.swap(list3,i,j);
                }
            }
        }

//        System.out.println(list3);

        List list=new ArrayList<>();
        for (int i = 0; i < list3.size(); i++) {
            Map tmp=new HashMap<>();
            tmp.put("from",list3.get(i).getFromUser());
            tmp.put("to",list3.get(i).getToUser());
            tmp.put("text",list3.get(i).getText());

            list.add(tmp);
        }

//        System.out.println(list1);

        returnObj=new ReturnObj("",list,"200");
        return returnObj;
    }


}
