package com.salemanager.modules.customer.service;

import com.salemanager.modules.customer.model.Customer;
import com.salemanager.modules.customer.param.CustomerParam;

import java.util.List;

/**
 * 客户服务接口
 */
public interface CustomerService {

    /**
     * 获取客户列表
     */
    List<Customer> getCustomerList(String keyword, Integer status, Integer page, Integer pageSize);

    /**
     * 获取客户总数
     */
    Long getCustomerCount(String keyword, Integer status);

    /**
     * 获取客户详情
     */
    Customer getCustomerById(Long id);

    /**
     * 新增客户
     */
    void createCustomer(CustomerParam param);

    /**
     * 更新客户
     */
    void updateCustomer(Long id, CustomerParam param);

    /**
     * 删除客户
     */
    void deleteCustomer(Long id);
}
