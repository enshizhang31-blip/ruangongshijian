package com.salemanager.modules.product.service;

import com.salemanager.modules.product.param.ProductMockDataParam;

import java.util.Map;

/**
 * 商品模拟数据服务接口
 */
public interface ProductMockDataService {

    /**
     * 生成模拟数据
     */
    Map<String, Object> generateMockData(ProductMockDataParam param);
}