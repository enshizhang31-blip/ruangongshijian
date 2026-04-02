package com.salemanager.modules.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.salemanager.modules.product.model.GoodsCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类Mapper
 */
@Mapper
public interface GoodsCategoryMapper extends BaseMapper<GoodsCategory> {
}
