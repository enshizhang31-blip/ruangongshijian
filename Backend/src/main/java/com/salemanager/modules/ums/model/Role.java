package com.salemanager.modules.ums.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色实体（预设模板）
 */
@Data
@TableName("role")
public class Role {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String code;
    private String description;
    private String permissions;
    private String routes;
    private Integer isPreset;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}