package com.example.springbootbank.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author baomidou
 * @since 2023-01-27
 */
@Getter
@Setter
  @TableName("sys_dict")
@ApiModel(value = "Dict对象", description = "")
public class Dict implements Serializable {

    private static final long serialVersionUID = 1L;

      @ApiModelProperty("名称")
      private String name;

      @ApiModelProperty("内容")
      private String value;

      @ApiModelProperty("类型")
      private String type;


}