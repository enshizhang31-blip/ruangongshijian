package com.salemanager.modules.customer.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("member_level_config")
public class MemberLevelConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer level;
    private String name;
    private BigDecimal consumeThreshold;
    private BigDecimal discount;
    private Integer pointsRate;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
