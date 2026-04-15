package com.salemanager.modules.ums.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.ums.mapper.AdminUserMapper;
import com.salemanager.modules.ums.mapper.RoleMapper;
import com.salemanager.modules.ums.model.AdminUser;
import com.salemanager.modules.ums.model.Role;
import com.salemanager.modules.ums.param.AdminUserParam;
import com.salemanager.modules.ums.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工服务实现
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<AdminUser> getUserList(String keyword, Integer status, Integer page, Integer pageSize) {
        log.info("getUserList keyword={}, status={}, page={}, pageSize={}", keyword, status, page, pageSize);

        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(AdminUser::getUsername, keyword)
                    .or()
                    .like(AdminUser::getRealName, keyword)
                    .or()
                    .like(AdminUser::getPhone, keyword));
        }

        if (status != null) {
            wrapper.eq(AdminUser::getStatus, status);
        }

        wrapper.orderByDesc(AdminUser::getCreatedAt);

        IPage<AdminUser> result = new Page<>(page, pageSize);
        adminUserMapper.selectPage(result, wrapper);

        return result.getRecords();
    }

    @Override
    public Long getUserCount(String keyword, Integer status) {
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(AdminUser::getUsername, keyword)
                    .or()
                    .like(AdminUser::getRealName, keyword)
                    .or()
                    .like(AdminUser::getPhone, keyword));
        }

        if (status != null) {
            wrapper.eq(AdminUser::getStatus, status);
        }

        return adminUserMapper.selectCount(wrapper);
    }

    @Override
    public AdminUser getUserById(Long id) {
        log.info("getUserById id={}", id);
        if (id == null || id <= 0) {
            throw new BusinessException(400, "员工ID无效");
        }

        AdminUser user = adminUserMapper.selectById(id);
        if (user == null) {
            log.warn("员工不存在 id={}", id);
            throw new BusinessException(404, "员工不存在");
        }
        // 不返回密码
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional
    public void createUser(AdminUserParam param) {
        log.info("createUser username={}", param.getUsername());

        // 校验用户名唯一
        Long count = adminUserMapper.selectCount(
                new LambdaQueryWrapper<AdminUser>()
                        .eq(AdminUser::getUsername, param.getUsername())
        );
        if (count > 0) {
            log.warn("用户名已存在 username={}", param.getUsername());
            throw new BusinessException(400, "用户名已存在");
        }

        // 从角色模板复制权限
        Role role = roleMapper.selectById(param.getRoleId());
        if (role == null) {
            log.warn("角色不存在 roleId={}", param.getRoleId());
            throw new BusinessException(400, "角色不存在");
        }

        AdminUser user = new AdminUser();
        user.setUsername(param.getUsername());
        user.setPassword(passwordEncoder.encode(param.getPassword()));
        user.setRealName(param.getRealName());
        user.setPhone(param.getPhone());
        user.setEmail(param.getEmail());
        user.setPermissions(role.getPermissions());
        user.setRoutes(role.getRoutes());
        user.setDepartmentId(param.getDepartmentId());
        user.setStatus(param.getStatus() != null ? param.getStatus() : 1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        adminUserMapper.insert(user);
        log.info("员工创建成功 id={}, username={}", user.getId(), user.getUsername());
    }

    @Override
    @Transactional
    public void updateUser(Long id, AdminUserParam param) {
        log.info("updateUser id={}", id);

        AdminUser user = adminUserMapper.selectById(id);
        if (user == null) {
            log.warn("员工不存在 id={}", id);
            throw new BusinessException(404, "员工不存在");
        }

        if (param.getRealName() != null) {
            user.setRealName(param.getRealName());
        }
        if (param.getPhone() != null) {
            user.setPhone(param.getPhone());
        }
        if (param.getEmail() != null) {
            user.setEmail(param.getEmail());
        }
        if (param.getStatus() != null) {
            user.setStatus(param.getStatus());
        }
        if (param.getDepartmentId() != null) {
            user.setDepartmentId(param.getDepartmentId());
        }

        user.setUpdatedAt(LocalDateTime.now());
        adminUserMapper.updateById(user);
        log.info("员工更新成功 id={}", id);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("deleteUser id={}", id);
        if (id == null || id <= 0) {
            throw new BusinessException(400, "员工ID无效");
        }

        AdminUser user = adminUserMapper.selectById(id);
        if (user == null) {
            log.warn("员工不存在 id={}", id);
            throw new BusinessException(404, "员工不存在");
        }
        adminUserMapper.deleteById(id);
        log.info("员工删除成功 id={}", id);
    }

    @Override
    @Transactional
    public void resetPassword(Long id) {
        log.info("resetPassword id={}", id);
        if (id == null || id <= 0) {
            throw new BusinessException(400, "员工ID无效");
        }

        AdminUser user = adminUserMapper.selectById(id);
        if (user == null) {
            log.warn("员工不存在 id={}", id);
            throw new BusinessException(404, "员工不存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        user.setUpdatedAt(LocalDateTime.now());
        adminUserMapper.updateById(user);
        log.info("密码重置成功 id={}", id);
    }

    @Override
    public Map<String, Object> getUserPermissions(Long id) {
        log.info("getUserPermissions id={}", id);
        if (id == null || id <= 0) {
            throw new BusinessException(400, "员工ID无效");
        }

        AdminUser user = adminUserMapper.selectById(id);
        if (user == null) {
            log.warn("员工不存在 id={}", id);
            throw new BusinessException(404, "员工不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("permissions", parseJson(user.getPermissions()));
        result.put("routes", parseJson(user.getRoutes()));
        return result;
    }

    @Override
    @Transactional
    public void updateUserPermissions(Long id, List<String> permissions, List<String> routes) {
        log.info("updateUserPermissions id={}, permissions={}, routes={}", id, permissions, routes);
        if (id == null || id <= 0) {
            throw new BusinessException(400, "员工ID无效");
        }

        AdminUser user = adminUserMapper.selectById(id);
        if (user == null) {
            log.warn("员工不存在 id={}", id);
            throw new BusinessException(404, "员工不存在");
        }

        user.setPermissions(permissions != null ? toJson(permissions) : null);
        user.setRoutes(routes != null ? toJson(routes) : null);
        user.setUpdatedAt(LocalDateTime.now());
        adminUserMapper.updateById(user);
        log.info("员工权限更新成功 id={}", id);
    }

    private String toJson(List<String> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            log.error("序列化失败", e);
            throw new BusinessException(500, "数据序列化失败");
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, List.class);
        } catch (JsonProcessingException e) {
            log.error("JSON解析失败: {}", json, e);
            return new ArrayList<>();
        }
    }
}