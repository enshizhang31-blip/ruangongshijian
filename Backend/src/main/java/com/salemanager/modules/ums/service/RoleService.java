package com.salemanager.modules.ums.service;

import com.salemanager.modules.ums.model.Role;

import java.util.List;

/**
 * 角色服务（仅读取预设角色）
 */
public interface RoleService {

    /**
     * 获取角色列表（仅预设角色）
     */
    List<Role> getRoleList();

    /**
     * 获取角色详情
     */
    Role getRoleById(Long id);
}