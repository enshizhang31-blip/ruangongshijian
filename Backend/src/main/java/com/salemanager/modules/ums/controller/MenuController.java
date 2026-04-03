package com.salemanager.modules.ums.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.ums.model.Menu;
import com.salemanager.modules.ums.service.MenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 */
@RestController
@RequestMapping("/api/admin/menu")
public class MenuController {

    private static final Logger log = LoggerFactory.getLogger(MenuController.class);

    @Autowired
    private MenuService menuService;

    /**
     * 获取菜单列表
     */
    @GetMapping
    public Result<List<Menu>> getMenuList() {
        log.info("getMenuList");
        List<Menu> list = menuService.getMenuList();
        return Result.success(list);
    }

    /**
     * 获取菜单树
     */
    @GetMapping("/tree")
    public Result<List<Menu>> getMenuTree() {
        log.info("getMenuTree");
        List<Menu> tree = menuService.getMenuTree();
        return Result.success(tree);
    }

    /**
     * 获取菜单详情
     */
    @GetMapping("/{id}")
    public Result<Menu> getMenuById(@PathVariable Long id) {
        log.info("getMenuById id={}", id);
        Menu menu = menuService.getMenuById(id);
        return Result.success(menu);
    }
}