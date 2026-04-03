package com.salemanager.modules.ums.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.ums.model.Role;
import com.salemanager.modules.ums.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色控制器（仅读取）
 */
@RestController
@RequestMapping("/api/admin/role")
public class RoleController {

    private static final Logger log = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private RoleService roleService;

    /**
     * 获取角色列表
     */
    @GetMapping
    public Result<List<Role>> getRoleList() {
        log.info("getRoleList");
        List<Role> list = roleService.getRoleList();
        return Result.success(list);
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    public Result<Role> getRoleById(@PathVariable Long id) {
        log.info("getRoleById id={}", id);
        Role role = roleService.getRoleById(id);
        return Result.success(role);
    }
}