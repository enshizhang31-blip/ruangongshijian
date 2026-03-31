package com.salemanager.modules.ums.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单实体
 */
@Data
@TableName("menu")
public class Menu {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String path;
    private String component;
    private String icon;
    private Integer sort;
    private Long parentId;
    private Integer type;
    private String permission;
    private Integer status;
    private LocalDateTime createdAt;

    /**
     * 子菜单（不映射到数据库）
     */
    @TableField(exist = false)
    private List<Menu> children;
}