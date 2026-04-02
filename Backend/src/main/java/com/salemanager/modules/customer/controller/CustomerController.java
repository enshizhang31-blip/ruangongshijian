package com.salemanager.modules.customer.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.customer.model.Customer;
import com.salemanager.modules.customer.param.CustomerParam;
import com.salemanager.modules.customer.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户控制器
 */
@RestController
@RequestMapping("/api/admin/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    /**
     * 获取客户列表
     */
    @GetMapping
    public Result<Map<String, Object>> getCustomerList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        List<Customer> list = customerService.getCustomerList(keyword, status, page, pageSize);
        Long total = customerService.getCustomerCount(keyword, status);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("pagination", Map.of(
                "page", page,
                "pageSize", pageSize,
                "total", total
        ));

        return Result.success(result);
    }

    /**
     * 获取客户详情
     */
    @GetMapping("/{id}")
    public Result<Customer> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        return Result.success(customer);
    }

    /**
     * 新增客户
     */
    @PostMapping
    public Result<Void> createCustomer(@RequestBody CustomerParam param) {
        customerService.createCustomer(param);
        return Result.success();
    }

    /**
     * 更新客户
     */
    @PutMapping("/{id}")
    public Result<Void> updateCustomer(@PathVariable Long id, @RequestBody CustomerParam param) {
        customerService.updateCustomer(id, param);
        return Result.success();
    }

    /**
     * 删除客户
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return Result.success();
    }
}
