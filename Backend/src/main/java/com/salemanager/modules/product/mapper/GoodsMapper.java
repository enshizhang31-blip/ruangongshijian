package com.salemanager.modules.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.salemanager.modules.product.model.Goods;
import org.apache.ibatis.annotations.Mapper;

/**
 * SPU商品Mapper
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
}
