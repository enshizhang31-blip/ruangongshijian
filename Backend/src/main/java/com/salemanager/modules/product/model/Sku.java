package com.salemanager.modules.product.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SKU规格实体
 */
@Data
@TableName("sku")
public class Sku {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long spuId;
    private String skuCode;
    private String specJson;
    private BigDecimal price;
    private BigDecimal costPrice;
    private String unit;
    private String imageUrl;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 动态填充：库存（在库SN码数量） */
    @TableField(exist = false)
    private Integer stock;
}
