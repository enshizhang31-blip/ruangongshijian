package com.salemanager.modules.ums.service;

import com.salemanager.modules.ums.model.AdminUser;
import com.salemanager.modules.ums.param.AdminUserParam;

import java.util.List;
import java.util.Map;

/**
 * 员工服务
 */
public interface UserService {

    /**
     * 获取员工列表
     */
    List<AdminUser> getUserList(String keyword, Integer status, Integer page, Integer pageSize);

    /**
     * 获取员工总数
     */
    Long getUserCount(String keyword, Integer status);

    /**
     * 获取员工详情
     */
    AdminUser getUserById(Long id);

    /**
     * 新增员工
     */
    void createUser(AdminUserParam param);

    /**
     * 更新员工
     */
    void updateUser(Long id, AdminUserParam param);

    /**
     * 删除员工
     */
    void deleteUser(Long id);

    /**
     * 重置密码
     */
    void resetPassword(Long id);

    /**
     * 获取员工权限
     */
    Map<String, Object> getUserPermissions(Long id);

    /**
     * 更新员工权限
     */
    void updateUserPermissions(Long id, List<String> permissions, List<String> routes);
}