package com.salemanager.modules.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salemanager.modules.customer.mapper.CustomerMapper;
import com.salemanager.modules.customer.model.Customer;
import com.salemanager.modules.customer.param.CustomerParam;
import com.salemanager.modules.customer.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户服务实现
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerMapper customerMapper;

    @Override
    public List<Customer> getCustomerList(String keyword, Integer status, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(Customer::getUsername, keyword)
                    .or()
                    .like(Customer::getRealName, keyword)
                    .or()
                    .like(Customer::getPhone, keyword));
        }

        if (status != null) {
            wrapper.eq(Customer::getStatus, status);
        }

        wrapper.orderByDesc(Customer::getCreatedAt);

        int offset = (page - 1) * pageSize;
        wrapper.last("LIMIT " + offset + ", " + pageSize);

        return customerMapper.selectList(wrapper);
    }

    @Override
    public Long getCustomerCount(String keyword, Integer status) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(Customer::getUsername, keyword)
                    .or()
                    .like(Customer::getRealName, keyword)
                    .or()
                    .like(Customer::getPhone, keyword));
        }

        if (status != null) {
            wrapper.eq(Customer::getStatus, status);
        }

        return customerMapper.selectCount(wrapper);
    }

    @Override
    public Customer getCustomerById(Long id) {
        return customerMapper.selectById(id);
    }

    @Override
    public void createCustomer(CustomerParam param) {
        Customer customer = new Customer();
        customer.setUsername(param.getUsername() != null ? param.getUsername() : param.getPhone());
        customer.setRealName(param.getRealName());
        customer.setPhone(param.getPhone());
        customer.setEmail(param.getEmail());
        customer.setAvatarUrl(param.getAvatarUrl());
        customer.setBalance(BigDecimal.ZERO);
        customer.setPoints(0);
        customer.setStatus(param.getStatus() != null ? param.getStatus() : 1);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());

        customerMapper.insert(customer);
    }

    @Override
    public void updateCustomer(Long id, CustomerParam param) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            return;
        }

        if (param.getRealName() != null) customer.setRealName(param.getRealName());
        if (param.getPhone() != null) customer.setPhone(param.getPhone());
        if (param.getEmail() != null) customer.setEmail(param.getEmail());
        if (param.getAvatarUrl() != null) customer.setAvatarUrl(param.getAvatarUrl());
        if (param.getStatus() != null) customer.setStatus(param.getStatus());
        customer.setUpdatedAt(LocalDateTime.now());

        customerMapper.updateById(customer);
    }

    @Override
    public void deleteCustomer(Long id) {
        customerMapper.deleteById(id);
    }
}
