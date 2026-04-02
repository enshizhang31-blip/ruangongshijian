package com.salemanager.modules.sale.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.salemanager.modules.sale.model.SaleOrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细Mapper
 */
@Mapper
public interface SaleOrderItemMapper extends BaseMapper<SaleOrderItem> {
}
