package com.salemanager.modules.ums.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.common.util.JwtUtil;
import com.salemanager.modules.ums.dto.LoginVO;
import com.salemanager.modules.ums.mapper.AdminUserMapper;
import com.salemanager.modules.ums.model.AdminUser;
import com.salemanager.modules.ums.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务实现
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public LoginVO login(String username, String password) {
        log.info("login username={}", username);

        // 查询用户
        AdminUser user = adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUser>()
                        .eq(AdminUser::getUsername, username)
        );

        if (user == null) {
            log.warn("登录失败：用户不存在 username={}", username);
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            log.warn("登录失败：账号已被禁用 username={}", username);
            throw new BusinessException(401, "账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("登录失败：密码错误 username={}", username);
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        adminUserMapper.updateById(user);

        log.info("登录成功 userId={}, username={}", user.getId(), username);

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
        log.info("getCurrentUser userId={}", userId);

        AdminUser user = adminUserMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在 userId={}", userId);
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