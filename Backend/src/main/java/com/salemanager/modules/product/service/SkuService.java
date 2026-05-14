package com.salemanager.modules.product.service;

import com.salemanager.modules.product.model.Sku;
import com.salemanager.modules.product.param.SkuParam;

import java.util.List;

/**
 * SKU服务接口
 */
public interface SkuService {

    /**
     * 获取商品SKU列表
     */
    List<Sku> getSkuListBySpuId(Long spuId);

    /**
     * 新增SKU
     */
    void createSku(SkuParam param);

    /**
     * 更新SKU
     */
    void updateSku(SkuParam param);

    /**
     * 删除SKU
     */
    void deleteSku(Long id);
}