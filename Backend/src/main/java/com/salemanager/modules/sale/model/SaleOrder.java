package com.salemanager.modules.sale.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体
 */
@Data
@TableName("`order`")
public class SaleOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private Long customerId;
    private String customerName;
    @TableField("receiver_phone")
    private String receiverPhone;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    @TableField("points_discount")
    private BigDecimal pointsDiscount;
    private BigDecimal payAmount;
    private Integer payType;
    @TableField("pay_time")
    private LocalDateTime paidAt;
    private Integer status;
    private Long addressId;
    @TableField("receiver_name")
    private String receiverName;
    @TableField("receiver_address")
    private String receiverAddress;
    private String remark;
    @TableField("cancel_time")
    private LocalDateTime cancelledAt;
    @TableField("finish_time")
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
