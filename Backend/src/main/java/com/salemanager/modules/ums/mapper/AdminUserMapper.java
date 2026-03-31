package com.salemanager.modules.ums.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.salemanager.modules.ums.model.AdminUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工Mapper
 */
@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
}