package com.salemanager.modules.ums.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.ums.mapper.RoleMapper;
import com.salemanager.modules.ums.model.Role;
import com.salemanager.modules.ums.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色服务实现
 */
@Service
public class RoleServiceImpl implements RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public List<Role> getRoleList() {
        log.info("getRoleList");
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
        log.info("getRoleById id={}", id);
        Role role = roleMapper.selectById(id);
        if (role == null) {
            log.warn("角色不存在 id={}", id);
            throw new BusinessException(404, "角色不存在");
        }
        return role;
    }
}