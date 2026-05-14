package com.salemanager.modules.product.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 规格值实体
 */
@Data
@TableName("spec_value")
public class SpecValue {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long specId;
    private String value;
    private Integer sort;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}