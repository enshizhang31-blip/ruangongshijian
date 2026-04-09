package com.salemanager.modules.ums.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.ums.model.Department;
import com.salemanager.modules.ums.service.DepartmentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门控制器
 */
@RestController
@RequestMapping("/api/admin/department")
@Validated
public class DepartmentController {

    private static final Logger log = LoggerFactory.getLogger(DepartmentController.class);

    @Autowired
    private DepartmentService departmentService;

    /**
     * 获取部门列表
     */
    @GetMapping
    public Result<List<Department>> getDepartmentList() {
        log.info("getDepartmentList");
        List<Department> list = departmentService.getDepartmentList();
        return Result.success(list);
    }

    /**
     * 获取部门详情
     */
    @GetMapping("/{id}")
    public Result<Department> getDepartmentById(@PathVariable Long id) {
        log.info("getDepartmentById id={}", id);
        Department department = departmentService.getDepartmentById(id);
        return Result.success(department);
    }

    /**
     * 创建部门
     */
    @PostMapping
    public Result<Void> createDepartment(@Valid @RequestBody Department department) {
        log.info("createDepartment name={}", department.getName());
        departmentService.createDepartment(department);
        return Result.success();
    }

    /**
     * 更新部门
     */
    @PutMapping("/{id}")
    public Result<Void> updateDepartment(@PathVariable Long id, @Valid @RequestBody Department department) {
        log.info("updateDepartment id={}", id);
        departmentService.updateDepartment(id, department);
        return Result.success();
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteDepartment(@PathVariable Long id) {
        log.info("deleteDepartment id={}", id);
        departmentService.deleteDepartment(id);
        return Result.success();
    }
}
