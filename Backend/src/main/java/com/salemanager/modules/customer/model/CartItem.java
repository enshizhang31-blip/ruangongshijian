package com.salemanager.modules.customer.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车明细实体，映射 cart 表。
 */
@Data
@TableName("cart")
public class CartItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long customerId;

    private Long skuId;

    @TableField(exist = false)
    private Long spuId;

    private Integer quantity;

    private Integer selected;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /** 关联查询：规格名 */
    @TableField(exist = false)
    private String spuName;

    @TableField(exist = false)
    private String skuCode;

    @TableField(exist = false)
    private String specJson;

    @TableField(exist = false)
    private BigDecimal price;

    @TableField(exist = false)
    private String imageUrl;

    @TableField(exist = false)
    private Integer stock;
}
