package com.salemanager.modules.ums.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.ums.mapper.DepartmentMapper;
import com.salemanager.modules.ums.model.Department;
import com.salemanager.modules.ums.service.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门服务实现
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    private static final Logger log = LoggerFactory.getLogger(DepartmentServiceImpl.class);

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public List<Department> getDepartmentList() {
        log.info("getDepartmentList");
        return departmentMapper.selectList(
                new LambdaQueryWrapper<Department>()
                        .eq(Department::getStatus, 1)
                        .orderByAsc(Department::getSort)
                        .orderByDesc(Department::getCreatedAt)
        );
    }

    @Override
    public Department getDepartmentById(Long id) {
        log.info("getDepartmentById id={}", id);
        if (id == null || id <= 0) {
            throw new BusinessException(400, "部门ID无效");
        }
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            log.warn("部门不存在 id={}", id);
            throw new BusinessException(404, "部门不存在");
        }
        return department;
    }

    @Override
    public void createDepartment(Department department) {
        log.info("createDepartment name={}", department.getName());
        if (!StringUtils.hasText(department.getName())) {
            throw new BusinessException(400, "部门名称不能为空");
        }
        department.setCreatedAt(LocalDateTime.now());
        department.setUpdatedAt(LocalDateTime.now());
        if (department.getStatus() == null) {
            department.setStatus(1);
        }
        departmentMapper.insert(department);
    }

    @Override
    public void updateDepartment(Long id, Department department) {
        log.info("updateDepartment id={}", id);
        if (id == null || id <= 0) {
            throw new BusinessException(400, "部门ID无效");
        }
        Department existing = departmentMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "部门不存在");
        }
        if (StringUtils.hasText(department.getName())) {
            existing.setName(department.getName());
        }
        if (department.getParentId() != null) {
            existing.setParentId(department.getParentId());
        }
        if (department.getSort() != null) {
            existing.setSort(department.getSort());
        }
        if (department.getStatus() != null) {
            existing.setStatus(department.getStatus());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        departmentMapper.updateById(existing);
    }

    @Override
    public void deleteDepartment(Long id) {
        log.info("deleteDepartment id={}", id);
        if (id == null || id <= 0) {
            throw new BusinessException(400, "部门ID无效");
        }
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException(404, "部门不存在");
        }
        departmentMapper.deleteById(id);
    }
}
