package com.salemanager.modules.ums.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.ums.model.AdminUser;
import com.salemanager.modules.ums.param.AdminUserParam;
import com.salemanager.modules.ums.param.AdminUserUpdateParam;
import com.salemanager.modules.ums.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工控制器
 */
@RestController
@RequestMapping("/api/admin/user")
@Validated
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * 获取员工列表
     */
    @GetMapping
    public Result<Map<String, Object>> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        log.info("getUserList keyword={}, status={}, page={}, pageSize={}", keyword, status, page, pageSize);

        List<AdminUser> list = userService.getUserList(keyword, status, page, pageSize);
        Long total = userService.getUserCount(keyword, status);

        // 不返回密码
        list.forEach(user -> user.setPassword(null));

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("pagination", Map.of(
                "page", page,
                "pageSize", pageSize,
                "total", total
        ));

        return Result.success(result);
    }

    /**
     * 获取员工详情
     */
    @GetMapping("/{id}")
    public Result<AdminUser> getUserById(@PathVariable Long id) {
        log.info("getUserById id={}", id);
        AdminUser user = userService.getUserById(id);
        return Result.success(user);
    }

    /**
     * 新增员工
     */
    @PostMapping
    public Result<Void> createUser(@Valid @RequestBody AdminUserParam param) {
        log.info("createUser username={}", param.getUsername());
        userService.createUser(param);
        return Result.success();
    }

    /**
     * 更新员工
     */
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @Valid @RequestBody AdminUserUpdateParam param) {
        log.info("updateUser id={}", id);
        userService.updateUser(id, param);
        return Result.success();
    }

    /**
     * 删除员工
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        log.info("deleteUser id={}", id);
        userService.deleteUser(id);
        return Result.success();
    }

    /**
     * 重置密码
     */
    @PostMapping("/{id}/resetpwd")
    public Result<Void> resetPassword(@PathVariable Long id) {
        log.info("resetPassword id={}", id);
        userService.resetPassword(id);
        return Result.success();
    }

    /**
     * 获取员工权限
     */
    @GetMapping("/{id}/permissions")
    public Result<Map<String, Object>> getUserPermissions(@PathVariable Long id) {
        log.info("getUserPermissions id={}", id);
        Map<String, Object> permissions = userService.getUserPermissions(id);
        return Result.success(permissions);
    }

    /**
     * 更新员工权限
     */
    @PutMapping("/{id}/permissions")
    public Result<Void> updateUserPermissions(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        log.info("updateUserPermissions id={}", id);
        @SuppressWarnings("unchecked")
        List<String> permissions = (List<String>) params.get("permissions");
        @SuppressWarnings("unchecked")
        List<String> routes = (List<String>) params.get("routes");
        userService.updateUserPermissions(id, permissions, routes);
        return Result.success();
    }
}