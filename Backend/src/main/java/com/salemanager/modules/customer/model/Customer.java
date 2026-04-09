package com.salemanager.modules.customer.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户/会员实体
 */
@Data
@TableName("customer")
public class Customer {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String openid;
    private String nickname;
    private String avatar;
    private String phone;
    private Integer memberLevel;
    private BigDecimal balance;
    private Integer points;
    private BigDecimal totalConsume;
    private Integer totalPoints;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
