package com.salemanager.modules.product.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SPU商品实体
 */
@Data
@TableName("goods")
public class Goods {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private Long categoryId;
    private String brand;
    private String imageUrl;
    private String images;
    private String shortDesc;
    private String description;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 动态填充：SKU数量 */
    @TableField(exist = false)
    private Integer skuCount;

    /** 动态填充：总库存（在库SN码数量） */
    @TableField(exist = false)
    private Integer stockCount;
}
