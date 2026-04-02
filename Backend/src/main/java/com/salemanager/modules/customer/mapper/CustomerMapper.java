package com.salemanager.modules.customer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.salemanager.modules.customer.model.Customer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户Mapper
 */
@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
}
