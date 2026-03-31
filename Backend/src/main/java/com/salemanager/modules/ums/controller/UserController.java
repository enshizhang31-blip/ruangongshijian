package com.salemanager.modules.ums.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.ums.model.AdminUser;
import com.salemanager.modules.ums.param.AdminUserParam;
import com.salemanager.modules.ums.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工控制器
 */
@RestController
@RequestMapping("/api/admin/user")
public class UserController {

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
            @RequestParam(defaultValue = "20") Integer pageSize) {

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
        AdminUser user = userService.getUserById(id);
        return Result.success(user);
    }

    /**
     * 新增员工
     */
    @PostMapping
    public Result<Void> createUser(@Valid @RequestBody AdminUserParam param) {
        userService.createUser(param);
        return Result.success();
    }

    /**
     * 更新员工
     */
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @Valid @RequestBody AdminUserParam param) {
        userService.updateUser(id, param);
        return Result.success();
    }

    /**
     * 删除员工
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    /**
     * 重置密码
     */
    @PostMapping("/{id}/resetpwd")
    public Result<Void> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success();
    }
}