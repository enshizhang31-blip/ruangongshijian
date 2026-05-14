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
     * @param keyword    搜索关键词
     * @param categoryId 分类ID（可选）
     * @param status     商品状态
     * @param page       页码
     * @param pageSize   每页数量
     * @return 商品列表（含SKU数和库存）
     */
    List<Goods> getProductList(String keyword, Long categoryId, Integer status, Integer page, Integer pageSize);

    /**
     * 获取商品总数
     *
     * @param keyword    搜索关键词
     * @param categoryId 分类ID（可选）
     * @param status     商品状态
     * @return 商品总数
     */
    Long getProductCount(String keyword, Long categoryId, Integer status);

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
     * 删除商品（级联删除SKU和SN码）
     *
     * @param id 商品ID
     */
    void deleteProduct(Long id);

    /**
     * 更新商品状态（上架/下架）
     * 上架前校验：必须有至少1个启用且库存>0的SKU
     *
     * @param id     商品ID
     * @param status 新状态
     */
    void updateProductStatus(Long id, Integer status);
}
