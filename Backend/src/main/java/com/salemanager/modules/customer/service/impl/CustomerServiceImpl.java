package com.salemanager.modules.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.customer.mapper.CustomerMapper;
import com.salemanager.modules.customer.model.Customer;
import com.salemanager.modules.customer.param.CustomerParam;
import com.salemanager.modules.customer.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户服务实现
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    @Autowired
    private CustomerMapper customerMapper;

    @Override
    public List<Customer> getCustomerList(String keyword, Integer status, Integer page, Integer pageSize) {
        log.info("getCustomerList keyword={}, status={}, page={}, pageSize={}", keyword, status, page, pageSize);

        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
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

        IPage<Customer> result = new Page<>(page, pageSize);
        customerMapper.selectPage(result, wrapper);

        return result.getRecords();
    }

    @Override
    public Long getCustomerCount(String keyword, Integer status) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
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
        log.info("getCustomerById id={}", id);
        if (id == null || id <= 0) {
            throw new BusinessException(400, "客户ID无效");
        }

        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            log.warn("客户不存在 id={}", id);
            throw new BusinessException("客户不存在");
        }
        return customer;
    }

    @Override
    @Transactional
    public void createCustomer(CustomerParam param) {
        log.info("createCustomer username={}", param.getUsername());

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
        log.info("客户创建成功 id={}", customer.getId());
    }

    @Override
    @Transactional
    public void updateCustomer(Long id, CustomerParam param) {
        log.info("updateCustomer id={}", id);

        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            log.warn("客户不存在 id={}", id);
            throw new BusinessException("客户不存在");
        }

        if (param.getRealName() != null) customer.setRealName(param.getRealName());
        if (param.getPhone() != null) customer.setPhone(param.getPhone());
        if (param.getEmail() != null) customer.setEmail(param.getEmail());
        if (param.getAvatarUrl() != null) customer.setAvatarUrl(param.getAvatarUrl());
        if (param.getStatus() != null) customer.setStatus(param.getStatus());
        customer.setUpdatedAt(LocalDateTime.now());

        customerMapper.updateById(customer);
        log.info("客户更新成功 id={}", id);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        log.info("deleteCustomer id={}", id);
        if (id == null || id <= 0) {
            throw new BusinessException(400, "客户ID无效");
        }

        Customer customer = customerMapper.selectById(id);
        if (customer == null) {
            log.warn("客户不存在 id={}", id);
            throw new BusinessException("客户不存在");
        }

        customerMapper.deleteById(id);
        log.info("客户删除成功 id={}", id);
    }
}
