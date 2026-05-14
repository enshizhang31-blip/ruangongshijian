package com.salemanager.modules.sale.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("balance_record")
public class BalanceRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long customerId;
    private Integer type;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private String source;
    private Long sourceId;
    private String remark;
    private LocalDateTime createdAt;
}
