package com.salemanager.modules.ums.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.ums.dto.LoginDTO;
import com.salemanager.modules.ums.dto.LoginVO;
import com.salemanager.modules.ums.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/admin/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO vo = authService.login(dto.getUsername(), dto.getPassword());
        return Result.success(vo);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public Result<LoginVO> getCurrentUser(@RequestHeader("X-User-Id") Long userId) {
        LoginVO vo = authService.getCurrentUser(userId);
        return Result.success(vo);
    }

    /**
     * 获取用户路由和权限
     */
    @GetMapping("/routes")
    public Result<LoginVO> getRoutes(@RequestHeader("X-User-Id") Long userId) {
        LoginVO vo = authService.getUserRoutes(userId);
        return Result.success(vo);
    }
}