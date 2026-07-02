package com.salemanager.modules.customer.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("customer")
public class Customer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String openid;
    private String nickname;
    private String avatar;
    private String phone;
    /** 演示版手机号密码登录用 SHA-256(原始密码+盐) */
    private String password;
    private Integer memberLevel;
    private BigDecimal balance;
    private Integer points;
    private Integer totalPoints;
    private BigDecimal totalConsume;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
