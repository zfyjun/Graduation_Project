package com.example.springbootbank.controller;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootbank.common.Constants;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.common.WebSocketServer;
import com.example.springbootbank.controller.dto.AdminDto;
import com.example.springbootbank.entity.Admin;
import com.example.springbootbank.entity.AdminOnline;
import com.example.springbootbank.entity.User;
import com.example.springbootbank.mapper.AdminMapper;
import com.example.springbootbank.mapper.AdminOnlineMapper;
import com.example.springbootbank.service.AdminOnlineService;
import com.example.springbootbank.service.IAdminService;
import com.example.springbootbank.utils.MD5Utils;
import org.springframework.jdbc.object.UpdatableSqlQuery;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author baomidou
 * @since 2023-01-16
 */
@RestController
@RequestMapping("/Admin")
public class AdminController {


    @Resource
    private IAdminService adminService;

    @Resource
    private AdminOnlineMapper adminOnlineMapper;

    @Resource
    private AdminMapper adminMapper;

//    private WebSocketServer webSocketServer=new WebSocketServer();

    @PostMapping("/login")
    public Result login(@RequestBody AdminDto adminDto){
        String username=adminDto.getUsername();
        String password=adminDto.getPassword();
        if(StrUtil.isBlank(username)||StrUtil.isBlank(password)){
            return Result.error(Constants.CODE_400,"参数错误");
        }
        adminDto.setPassword(MD5Utils.md5(adminDto.getPassword()));
        AdminDto dto=adminService.login(adminDto);

        return Result.success(dto);
    }

    @PostMapping("/register")
    public Result register(@RequestBody AdminDto adminDto){
        System.out.println(adminDto);
        adminDto.setName(adminDto.getUsername());
        adminDto.setPassword(MD5Utils.md5(adminDto.getPassword()));
        adminDto.setRole("ROLE_USER");

        String username=adminDto.getUsername();
        String password=adminDto.getPassword();
        if(StrUtil.isBlank(username)||StrUtil.isBlank(password)){
            return Result.error(Constants.CODE_400,"参数错误");
        }
        return Result.success(adminService.register(adminDto));
    }

    @PostMapping("/update")
    public Result update(@RequestBody Admin user){
        adminService.updateById(user);
        QueryWrapper<AdminOnline> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("role",1);
        List<Integer> list=new ArrayList<>();
        List<AdminOnline> adminOnlineList = adminOnlineMapper.selectList(queryWrapper);
        for (int i = 0; i < adminOnlineList.size(); i++) {
            list.add(adminOnlineList.get(i).getId());
        }
        if (!list.isEmpty()){
            adminOnlineMapper.deleteBatchIds(list);
        }


        QueryWrapper<Admin> queryWrapper1=new QueryWrapper<>();
        queryWrapper1.eq("role","ROLE_SERVICE");
//        queryWrapper1.eq("role","ROLE_ROOT");
        List<Admin> adminList=adminMapper.selectList(queryWrapper1);
        System.out.println(adminList);
        for (int i = 0; i < adminList.size(); i++) {
            AdminOnline adminOnline=new AdminOnline();
            adminOnline.setU_id(adminList.get(i).getId());
            adminOnline.setRole(1);
            adminOnline.setOnline(0);
            adminOnline.setName(adminList.get(i).getName());
            adminOnlineMapper.insert(adminOnline);
        }
        AdminOnline adminOnline=new AdminOnline();
        adminOnline.setU_id(1);
        adminOnline.setRole(1);
        adminOnline.setOnline(0);
        adminOnline.setName("root");
        adminOnlineMapper.insert(adminOnline);
//        webSocketServer.init();

        return Result.success();
    }
    //新增或更新
    @PostMapping
    public Result save(@RequestBody Admin user){
            return Result.success(adminService.saveOrUpdate(user));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(adminService.removeById(id));
    }

    @GetMapping
    public Result findAll() {
            return Result.success(adminService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
            return Result.success(adminService.getById(id));
    }

    @GetMapping("/username/{username}")
    public Result findOne(@PathVariable String username) {
        QueryWrapper<Admin> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("username",username);
        return Result.success(adminService.getOne(queryWrapper));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String name) {
            QueryWrapper<Admin> queryWrapper=new QueryWrapper<>();
            queryWrapper.like("username",name);
            return Result.success(adminService.page(new Page<>(pageNum, pageSize),queryWrapper));
    }

    /**
     * excel 导出
     *
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response)throws Exception{
        List<Admin> list=adminService.list();
        //通过工具类writer写出到磁盘路径
        ExcelWriter writer= ExcelUtil.getWriter(true);
        writer.addHeaderAlias("id","id");
        writer.addHeaderAlias("username","用户名");
        writer.addHeaderAlias("password","密码");

        //一次性写出list内的对象到excel,使用默认样式，强制输出标题
        writer.write(list,true);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName= URLEncoder.encode("用户信息","UTF-8");
        response.setHeader("Content-Disposition","attachment;filename="+fileName+".xlsx");

        ServletOutputStream out =response.getOutputStream();
        writer.flush(out,true);
        out.close();
        writer.close();
    }

    /**
     * excel导入
     * */
    @PostMapping("import")
    public Result imp(MultipartFile file) throws Exception{
        InputStream inputStream =file.getInputStream();
        ExcelReader reader=ExcelUtil.getReader(inputStream);
        //通过javabean的方式读取对象，要求表头是英文
//        List<User> list=reader.readAll(User.class);

        //忽略表头的中文
        List<List<Object>> list=reader.read(1);
        List<Admin> users = CollUtil.newArrayList();
        for(List<Object> row :list){
            Admin user =new Admin();
            user.setUsername(row.get(0).toString());
            user.setPassword(row.get(1).toString());

            users.add(user);
        }
        System.out.println(list);
        adminService.saveBatch(users);
        return Result.success(true);
    }

}

