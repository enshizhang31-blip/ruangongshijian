package com.salemanager.modules.sn.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SN码实体
 */
@Data
@TableName("sn_code")
public class SnCode {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String snCode;
    private Long skuId;
    private Long spuId;
    private String spuName;
    private String skuCode;
    private String specJson;
    private BigDecimal price;
    private Integer status;
    private Integer source;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime soldAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
