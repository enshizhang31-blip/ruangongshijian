package com.salemanager.modules.sn.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * SN码操作日志实体
 */
@Data
@TableName("sn_code_log")
public class SnCodeLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long snCodeId;
    private String snCode;
    private Long skuId;
    private String operation;
    private Integer fromStatus;
    private Integer toStatus;
    private Long operatorId;
    private String operatorName;
    private String remark;
    private LocalDateTime createdAt;
}
