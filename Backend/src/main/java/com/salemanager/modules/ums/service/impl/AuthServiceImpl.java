package com.salemanager.modules.ums.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.common.util.JwtUtil;
import com.salemanager.modules.ums.dto.LoginVO;
import com.salemanager.modules.ums.mapper.AdminUserMapper;
import com.salemanager.modules.ums.model.AdminUser;
import com.salemanager.modules.ums.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务实现
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public LoginVO login(String username, String password) {
        // 查询用户
        AdminUser user = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>()
                        .eq(AdminUser::getUsername, username)
        );

        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(401, "账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        adminUserMapper.updateById(user);

        // 构建返回
        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setToken(token);
        vo.setPermissions(user.getPermissions());
        vo.setRoutes(user.getRoutes());

        return vo;
    }

    @Override
    public LoginVO getCurrentUser(Long userId) {
        AdminUser user = adminUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPermissions(user.getPermissions());
        vo.setRoutes(user.getRoutes());

        return vo;
    }

    @Override
    public LoginVO getUserRoutes(Long userId) {
        return getCurrentUser(userId);
    }
}