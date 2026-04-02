package com.salemanager.modules.ums.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.ums.mapper.AdminUserMapper;
import com.salemanager.modules.ums.mapper.RoleMapper;
import com.salemanager.modules.ums.model.AdminUser;
import com.salemanager.modules.ums.model.Role;
import com.salemanager.modules.ums.param.AdminUserParam;
import com.salemanager.modules.ums.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 员工服务实现
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<AdminUser> getUserList(String keyword, Integer status, Integer page, Integer pageSize) {
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
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

        int offset = (page - 1) * pageSize;
        wrapper.last("LIMIT " + offset + ", " + pageSize);

        return adminUserMapper.selectList(wrapper);
    }

    @Override
    public Long getUserCount(String keyword, Integer status) {
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
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
        AdminUser user = adminUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "员工不存在");
        }
        // 不返回密码
        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional
    public void createUser(AdminUserParam param) {
        // 校验用户名唯一
        Long count = adminUserMapper.selectCount(
                new LambdaQueryWrapper<AdminUser>()
                        .eq(AdminUser::getUsername, param.getUsername())
        );
        if (count > 0) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 从角色模板复制权限
        Role role = roleMapper.selectById(param.getRoleId());
        if (role == null) {
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
    }

    @Override
    @Transactional
    public void updateUser(Long id, AdminUserParam param) {
        AdminUser user = adminUserMapper.selectById(id);
        if (user == null) {
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
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        AdminUser user = adminUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "员工不存在");
        }
        adminUserMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void resetPassword(Long id) {
        AdminUser user = adminUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "员工不存在");
        }
        user.setPassword(passwordEncoder.encode("123456"));
        user.setUpdatedAt(LocalDateTime.now());
        adminUserMapper.updateById(user);
    }
}