package com.salemanager.modules.ums.service;

import com.salemanager.modules.ums.model.Menu;

import java.util.List;

/**
 * 菜单服务
 */
public interface MenuService {

    /**
     * 获取菜单列表
     */
    List<Menu> getMenuList();

    /**
     * 获取菜单树
     */
    List<Menu> getMenuTree();

    /**
     * 获取菜单详情
     */
    Menu getMenuById(Long id);
}