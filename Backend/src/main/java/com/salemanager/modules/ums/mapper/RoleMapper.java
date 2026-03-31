package com.salemanager.modules.ums.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.salemanager.modules.ums.model.Role;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色Mapper
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}