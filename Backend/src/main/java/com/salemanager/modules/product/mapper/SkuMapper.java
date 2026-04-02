package com.salemanager.modules.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.salemanager.modules.product.model.Sku;
import org.apache.ibatis.annotations.Mapper;

/**
 * SKU规格Mapper
 */
@Mapper
public interface SkuMapper extends BaseMapper<Sku> {
}
