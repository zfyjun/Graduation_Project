package com.example.springbootbank.utils;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;

public class HDFSClient {
   static FileSystem fileSystem;

    static {
        Configuration conf=new Configuration();

//        设置副本数
        conf.set("dfs.replication","2");
        try {
            fileSystem=FileSystem.get(URI.create("hdfs://192.168.207.101:9000"),conf,"root");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }


    public static void mkdir(){
        try {
            boolean mkdirs = fileSystem.mkdirs(new Path("/test2"));
            if(mkdirs){
                System.out.println("创建成功");
            }else {
                System.out.println("创建失败");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }


   public static void main(String[] args) {
        mkdir();
    }

}
