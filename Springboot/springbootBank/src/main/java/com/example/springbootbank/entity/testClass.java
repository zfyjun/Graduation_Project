package com.example.springbootbank.entity;

import lombok.Data;

@Data
public class testClass {
    private Integer id;
    private String title;
    private String content;
    private String img;
    private String category;
    private String type;
    private String time;
    private Integer adminId;
    private Integer readCount;
}
