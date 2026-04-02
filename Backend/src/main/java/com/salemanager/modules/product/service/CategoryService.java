package com.salemanager.modules.product.service;

import com.salemanager.modules.product.model.GoodsCategory;

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
     */
    GoodsCategory getCategoryById(Long id);

    /**
     * 新增分类
     */
    void createCategory(GoodsCategory category);

    /**
     * 更新分类
     */
    void updateCategory(Long id, GoodsCategory category);

    /**
     * 删除分类
     */
    void deleteCategory(Long id);
}
