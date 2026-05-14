package com.salemanager.modules.sale.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("points_record")
public class PointsRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long customerId;
    private Integer type;
    private Integer amount;
    private Integer balance;
    private String source;
    private Long sourceId;
    private String remark;
    private LocalDateTime createdAt;
}
