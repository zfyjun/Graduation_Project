package com.example.springbootbank.common;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.springbootbank.controller.AdminOnlineController;
import com.example.springbootbank.entity.AdminOnline;
import com.example.springbootbank.entity.ChatHistory;
import com.example.springbootbank.mapper.AdminOnlineMapper;
import com.example.springbootbank.mapper.ChatHistoryMapper;
import com.example.springbootbank.service.AdminOnlineService;
import com.example.springbootbank.utils.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.object.UpdatableSqlQuery;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author websocket服务
 */
@ServerEndpoint(value = "/imserver/{username}")
@Component
public class WebSocketServer {
    @Resource
    private AdminOnlineMapper adminOnlineMapper;

    @Resource
    private ChatHistoryMapper chatHistoryMapper;

    // 用于存储所有用户
    private static List<AdminOnline> allUsers=new ArrayList<>();
    private JSONObject UnReadObject=new JSONObject();

    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);
    /**
     * 记录当前在线连接数
     */
    // 用于存储所有在线用户
    public static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();


    @PostConstruct
    public void init() {
        QueryWrapper<AdminOnline> queryWrapper=new QueryWrapper<>();
        queryWrapper.select("*");

        List<AdminOnline> list=adminOnlineMapper.selectList(queryWrapper);
        allUsers = list;
//        System.out.println("init");
//        System.out.println(allUsers);
    }

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) {
        sessionMap.put(username, session);

        // 设置该用户在线
        allUsers.stream()
                .filter(user -> user.getName().equals(username))
                .findFirst()
                .ifPresent(user -> user.setOnline(1));

        AdminOnlineService adminOnlineService = SpringContextUtil.getBean(AdminOnlineService.class);
        adminOnlineService.setOnline(username);

        log.info("有新用户加入，username={}, 当前在线人数为：{}", username, sessionMap.size());
        // 构建要发送的消息
        JSONObject result = new JSONObject();
        JSONArray array = new JSONArray();

        // 把userList的每个用户都添加到usersArray中
        for (AdminOnline user : allUsers) {
            JSONObject userObject = new JSONObject();
            userObject.set("username", user.getName());
            userObject.set("isOnline", user.getOnline());
            userObject.set("role",user.getRole());

            array.add(userObject);
        }
        result.set("users", array);

//        {"users": [{"username": "zhang"},{ "username": "admin"}]}
        sendAllMessage(JSONUtil.toJsonStr(result));  // 后台发送消息给所有的客户端
    }
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session, @PathParam("username") String username) {
        sessionMap.remove(username);
        log.info("有一连接关闭，移除username={}的用户session, 当前在线人数为：{}", username, sessionMap.size());

        // 设置该用户离线
        allUsers.stream()
                .filter(user -> user.getName().equals(username))
                .findFirst()
                .ifPresent(user -> user.setOnline(0));
//        UpdateWrapper<AdminOnline> updateWrapper=new UpdateWrapper<>();
//        updateWrapper.eq("name",username).set("online",0);
//        adminOnlineMapper.update(null,updateWrapper);
        //同步数据库中的状态变化
        AdminOnlineService adminOnlineService = SpringContextUtil.getBean(AdminOnlineService.class);
        adminOnlineService.setOffline(username);

        JSONObject result = new JSONObject();
        JSONArray array = new JSONArray();

        // 把userList的每个用户都添加到usersArray中
        for (AdminOnline user : allUsers) {
            JSONObject userObject = new JSONObject();
            userObject.set("username", user.getName());
            userObject.set("isOnline", user.getOnline());
            userObject.set("role",user.getRole());
            array.add(userObject);
        }
        result.set("users", array);
        sendAllMessage(JSONUtil.toJsonStr(result));  // 后台发送消息给所有的客户端

    }
    /**
     * 收到客户端消息后调用的方法
     * 后台收到客户端发送过来的消息
     * onMessage 是一个消息的中转站
     * 接受 浏览器端 socket.send 发送过来的 json数据
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("username") String username) {
        log.info("服务端收到用户username={}的消息:{}", username, message);
        JSONObject obj = JSONUtil.parseObj(message);


        String toUsername = obj.getStr("to"); // to表示发送给哪个用户，比如 admin
        String text = obj.getStr("text"); // 发送的消息文本  hello
        // {"to": "admin", "text": "聊天文本"}
        Session toSession = sessionMap.get(toUsername); // 根据 to用户名来获取 session，再通过session发送消息文本
        if (toSession != null) {
            // 服务器端 再把消息组装一下，组装后的消息包含发送人和发送的文本内容
            // {"from": "zhang", "text": "hello"}
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("from", username);  // from 是 zhang
            jsonObject.set("text", text);  // text 同上面的text
            this.sendMessage(jsonObject.toString(), toSession);
            log.info("发送给用户username={}，消息：{}", toUsername, jsonObject.toString());
        } else {
            log.info("发送失败，未找到用户username={}的session", toUsername);
        }
    }
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }
    /**
     * 服务端发送消息给客户端
     */
    private void sendMessage(String message, Session toSession) {
        try {
            log.info("服务端给客户端[{}]发送消息{}", toSession.getId(), message);
            toSession.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("服务端发送消息给客户端失败", e);
        }
    }
    /**
     * 服务端发送消息给所有客户端
     */
    private void sendAllMessage(String message) {
        try {
            for (Session session : sessionMap.values()) {
                log.info("服务端给客户端[{}]发送消息{}", session.getId(), message);
                session.getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            log.error("服务端发送消息给客户端失败", e);
        }
    }
}