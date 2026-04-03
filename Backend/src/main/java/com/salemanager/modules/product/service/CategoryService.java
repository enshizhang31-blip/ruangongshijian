package com.salemanager.modules.product.service;

import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.product.model.GoodsCategory;
import com.salemanager.modules.product.param.CategoryParam;

import java.util.List;

/**
 * 商品分类服务接口
 */
public interface CategoryService {

    /**
     * 获取分类列表
     */
    List<GoodsCategory> getCategoryList();

    /**
     * 获取分类详情
     *
     * @param id 分类ID
     * @return 分类信息
     * @throws BusinessException 分类不存在时抛出异常
     */
    GoodsCategory getCategoryById(Long id);

    /**
     * 新增分类
     */
    void createCategory(CategoryParam param);

    /**
     * 更新分类
     */
    void updateCategory(Long id, CategoryParam param);

    /**
     * 删除分类
     */
    void deleteCategory(Long id);
}
