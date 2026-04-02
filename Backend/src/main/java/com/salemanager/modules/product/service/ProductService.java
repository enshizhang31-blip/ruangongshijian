package com.salemanager.modules.product.service;

import com.salemanager.modules.product.model.Goods;
import com.salemanager.modules.product.param.ProductParam;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 获取商品列表
     */
    List<Goods> getProductList(String keyword, Integer status, Integer page, Integer pageSize);

    /**
     * 获取商品总数
     */
    Long getProductCount(String keyword, Integer status);

    /**
     * 获取商品详情
     */
    Goods getProductById(Long id);

    /**
     * 新增商品
     */
    void createProduct(ProductParam param);

    /**
     * 更新商品
     */
    void updateProduct(Long id, ProductParam param);

    /**
     * 删除商品
     */
    void deleteProduct(Long id);
}
