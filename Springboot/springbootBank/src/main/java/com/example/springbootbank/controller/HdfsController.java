//package com.example.springbootbank.controller;
//
//import com.baomidou.mybatisplus.core.toolkit.StringUtils;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.hadoop.fs.FileSystem;
//import org.apache.hadoop.fs.Path;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.io.IOException;
//
///**
// * @Author: best_liu
// * @Description:
// * @Date Create in 14:20 2023/4/21
// * @Modified By:
// */
//@RequestMapping("/hdfs")
//@RestController
//@Slf4j
//public class HdfsController {
//    @Value("${hadoop.name-node}")
//    private String nameNode;
//    @Value("${hadoop.namespace:/}")
//    private String nameSpace;
//
//    @Autowired
//    private FileSystem fileSystem;
//
//
//    /*** 将本地文件srcFile,上传到hdfs
//     * @param srcFile
//     * @return
//     */
//    @PostMapping("/upload")
//    public String upload( String srcFile){
//        srcFile = "D:\\test.txt";
//        uploadFile(srcFile);
//        return "upload";
//    }
//
//    public void uploadFile(String srcFile){
//        this.copyFileToHDFS(false,true,srcFile,nameSpace);
//    }
//    public  void copyFileToHDFS(boolean delSrc, boolean overwrite,String srcFile,String destPath) {
//        // 源文件路径是Linux下的路径，如果在 windows 下测试，需要改写为Windows下的路径，比如D://hadoop/djt/weibo.txt
//        Path srcPath = new Path(srcFile);
//        // 目的路径
//        if(StringUtils.isNotBlank(nameNode)){
//            destPath = nameNode + destPath;
//        }
//        Path dstPath = new Path(destPath);
//        // 实现文件上传
//        try {
//            // 获取FileSystem对象
//            fileSystem.copyFromLocalFile(srcPath, dstPath);
//            fileSystem.copyFromLocalFile(delSrc,overwrite,srcPath, dstPath);
//            //释放资源//
////            fileSystem.close();
//        } catch (IOException e) {
//            log.error("", e);
//        }
//    }
//
//    @PostMapping("/delFile")
//    public String del(String fileName){
//        rmdir(nameSpace,"test.txt") ;
//        return "delFile";
//    }
//
//    public void rmdir(String path,String fileName) {
//        try {
//            // 返回FileSystem对象
//            if(StringUtils.isNotBlank(nameNode)){
//                path = nameNode + path;
//            }
//            if(StringUtils.isNotBlank(fileName)){
//                path =  path + "/" +fileName;
//            }
//            // 删除文件或者文件目录  delete(Path f) 此方法已经弃用
//            fileSystem.delete(new Path(path),true);
//        } catch (IllegalArgumentException | IOException e) {
//            log.error("", e);
//        }
//    }
//
//    @PostMapping("/download")
//    public String download(String fileName,String savePath){
//        getFile(nameSpace+"/"+"test.txt","D:\\work\\lxjTest\\hadoopmaster");
//        return "download";
//    }
//    /*** 从 HDFS 下载文件
//     ** @param hdfsFile
//     * @param destPath 文件下载后,存放地址
//     */
//    public void getFile(String hdfsFile,String destPath) {
//        // 源文件路径
//        if(StringUtils.isNotBlank(nameNode)){
//            hdfsFile = nameNode + hdfsFile;
//        }
//        Path hdfsPath = new Path(hdfsFile);
//        Path dstPath = new Path(destPath);
//        try {
//            // 下载hdfs上的文件
//            fileSystem.copyToLocalFile(hdfsPath, dstPath);
//            // 释放资源//
//            fileSystem.close();
//        } catch (IOException e) {
//            log.error("", e);
//        }
//    }
//}