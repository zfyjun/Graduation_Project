package com.example.springbootbank.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.springbootbank.common.IdGeneratorSnowlake;
import com.example.springbootbank.common.Result;
import com.example.springbootbank.entity.Files;
import com.example.springbootbank.entity.Market;
import com.example.springbootbank.mapper.FilesMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.spring.web.json.Json;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController {
    @Value("${files.upload.path}")
    private String fileUpLoadPath;

    @Resource
    private FilesMapper filesMapper;

    private static final String ROOT_PATH=System.getProperty("user.dir")+File.separator+"files";


//    @Value("${server.port}")
//    private String port;

//    @Value("${ip:localhost}")
//    private String ip;

    @PostMapping("/upload")
    public Result upload(@RequestParam MultipartFile file) throws IOException {
        String originalFilename= file.getOriginalFilename();
        String type = FileUtil.extName(originalFilename);
        long size = file.getSize();
        //判断文件路径是否存在，若不存在新建一个目录

        String uuid= IdUtil.fastSimpleUUID();
        String fileUUid=uuid+ StrUtil.DOT+type;
        File uploadFile=new File(ROOT_PATH+File.separator+fileUUid);
        File parentFile=uploadFile.getParentFile();
        if(!parentFile.exists()){
            parentFile.mkdirs();
        }
        String md5;
        String url;
        file.transferTo(uploadFile);
        md5= SecureUtil.md5(uploadFile);
        Files dbfiles=getFileByMd5(md5);
        if(dbfiles!=null){
            url=dbfiles.getUrl();
            uploadFile.delete();
        }else{
            url="http://localhost:9090/file/download/"+fileUUid;
        }
        Files saveFile=new Files();
        saveFile.setName(originalFilename);
        saveFile.setType(type);
        saveFile.setSize(size/1024);
        saveFile.setUrl(url);
        saveFile.setMd5(md5);
        filesMapper.insert(saveFile);
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("url",url);
        jsonObject.put("id",saveFile.getId());
        return Result.success(jsonObject);
    }


    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) throws IOException {
        response.addHeader("Content-Disposition","attachment;filename="+ URLEncoder.encode(fileName,"UTF-8"));
        String filePath=ROOT_PATH+File.separator+fileName;
        if(!FileUtil.exist(filePath)){
            return ;
        }
        byte[] bytes=FileUtil.readBytes(filePath);
        ServletOutputStream outputStream=response.getOutputStream();
        outputStream.write(bytes);
        outputStream.flush();
        outputStream.close();
    }
    @PostMapping("/filesDeletById")//根据id删除文件
    public Result getMarketDatebyId(@RequestBody Map map){
        Integer id=(Integer) map.get("id");
        Files files=filesMapper.selectById(id);
        files.setIsDelete(true);
        if(filesMapper.updateById(files)==1){
            return Result.success();
        }
        return Result.error("500","文件删除失败");
    }
    @PostMapping("/filesselectByIds")//根据id数组获取文件
    public Result filesselectByIds(@RequestBody Map map){
        String idstr =(String) map.get("ids");
        List<Integer> ids= JSONArray.parseArray(idstr,Integer.class);
        List<Files> filesList=new ArrayList<>();
        for(int i=0;i<ids.size();i++){
            Files filesone=filesMapper.selectById(ids.get(i));
            filesList.add(filesone);
        }
        if(filesList.size()>0){
            return Result.success(filesList);
        }
        return Result.error("500","文件删除失败");
    }
    public Files getFileByMd5(String md5){
        QueryWrapper<Files> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("md5",md5);
        List<Files> filesList=filesMapper.selectList(queryWrapper);
        return filesList.size()==0 ? null:filesList.get(0);
    }

    @PostMapping("/editor/upload")
    public Dict editorUpload(@RequestParam MultipartFile file,@RequestParam String type) throws IOException {

        String originalFilename=file.getOriginalFilename();
        String mainName=FileUtil.mainName(originalFilename);
        String extName=FileUtil.extName(originalFilename);
        if(!FileUtil.exist(ROOT_PATH)){
            FileUtil.mkdir(ROOT_PATH);
        }
        if(FileUtil.exist(ROOT_PATH+File.separator+originalFilename)){
            originalFilename=System.currentTimeMillis()+"_"+mainName+"."+extName;
        }
        File saveFile=new File(ROOT_PATH+File.separator+originalFilename);
        file.transferTo(saveFile);
        String url="http://localhost:9090/files/download/"+originalFilename;
        if("img".equals(type)){
            return Dict.create().set("errno",0).set("data",CollUtil.newArrayList(Dict.create().set("url",url)));
        }else if("video".equals(type)){
            return Dict.create().set("errno",0).set("data",Dict.create().set("url",url));
        }
        return Dict.create().set("errno",0);}


}
