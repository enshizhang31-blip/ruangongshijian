package com.salemanager.modules.ums.service;

import com.salemanager.modules.ums.dto.LoginVO;

/**
 * 认证服务
 */
public interface AuthService {

    /**
     * 登录
     */
    LoginVO login(String username, String password);

    /**
     * 获取当前用户信息
     */
    LoginVO getCurrentUser(Long userId);

    /**
     * 获取用户路由和权限
     */
    LoginVO getUserRoutes(Long userId);
}