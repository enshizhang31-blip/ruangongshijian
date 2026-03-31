package com.salemanager.modules.ums.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.ums.mapper.RoleMapper;
import com.salemanager.modules.ums.model.Role;
import com.salemanager.modules.ums.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色服务实现
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public List<Role> getRoleList() {
        // 只查询预设角色
        return roleMapper.selectList(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getIsPreset, 1)
                        .eq(Role::getStatus, 1)
                        .orderByAsc(Role::getId)
        );
    }

    @Override
    public Role getRoleById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }
        return role;
    }
}