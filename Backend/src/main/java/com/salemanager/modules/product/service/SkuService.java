package com.salemanager.modules.product.service;

import com.salemanager.modules.product.model.Sku;
import com.salemanager.modules.product.param.BatchGenerateSkuParam;
import com.salemanager.modules.product.param.SkuParam;

import java.util.List;

/**
 * SKU服务接口
 */
public interface SkuService {

    /**
     * 获取商品SKU列表（含动态库存）
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
     * 删除SKU（校验是否有在库SN码）
     */
    void deleteSku(Long id);

    /**
     * 批量生成SKU（笛卡尔积）
     */
    List<Sku> batchGenerateSkus(BatchGenerateSkuParam param);
}