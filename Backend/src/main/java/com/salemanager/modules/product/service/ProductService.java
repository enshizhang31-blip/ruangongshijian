package com.salemanager.modules.product.service;

import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.product.model.Goods;
import com.salemanager.modules.product.param.ProductParam;

import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 获取商品列表
     *
     * @param keyword  搜索关键词
     * @param status   商品状态
     * @param page     页码
     * @param pageSize 每页数量
     * @return 商品列表
     */
    List<Goods> getProductList(String keyword, Integer status, Integer page, Integer pageSize);

    /**
     * 获取商品总数
     *
     * @param keyword 搜索关键词
     * @param status  商品状态
     * @return 商品总数
     */
    Long getProductCount(String keyword, Integer status);

    /**
     * 获取商品详情
     *
     * @param id 商品ID
     * @return 商品信息
     * @throws BusinessException 商品不存在时抛出异常
     */
    Goods getProductById(Long id);

    /**
     * 新增商品
     *
     * @param param 商品参数
     */
    void createProduct(ProductParam param);

    /**
     * 更新商品
     *
     * @param id    商品ID
     * @param param 商品参数
     */
    void updateProduct(Long id, ProductParam param);

    /**
     * 删除商品
     *
     * @param id 商品ID
     */
    void deleteProduct(Long id);
}
