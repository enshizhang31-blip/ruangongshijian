package com.salemanager.modules.ums.service;

import com.salemanager.modules.ums.model.Department;

import java.util.List;

/**
 * 部门服务
 */
public interface DepartmentService {

    /**
     * 获取部门列表
     */
    List<Department> getDepartmentList();

    /**
     * 获取部门详情
     */
    Department getDepartmentById(Long id);

    /**
     * 创建部门
     */
    void createDepartment(Department department);

    /**
     * 更新部门
     */
    void updateDepartment(Long id, Department department);

    /**
     * 删除部门
     */
    void deleteDepartment(Long id);
}
