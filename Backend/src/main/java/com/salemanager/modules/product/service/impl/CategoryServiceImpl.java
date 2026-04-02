package com.salemanager.modules.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salemanager.modules.product.mapper.GoodsCategoryMapper;
import com.salemanager.modules.product.model.GoodsCategory;
import com.salemanager.modules.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品分类服务实现
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private GoodsCategoryMapper categoryMapper;

    @Override
    public List<GoodsCategory> getCategoryList() {
        LambdaQueryWrapper<GoodsCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(GoodsCategory::getSort);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public GoodsCategory getCategoryById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public void createCategory(GoodsCategory category) {
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.insert(category);
    }

    @Override
    public void updateCategory(Long id, GoodsCategory category) {
        GoodsCategory existing = categoryMapper.selectById(id);
        if (existing == null) {
            return;
        }

        if (category.getName() != null) existing.setName(category.getName());
        if (category.getParentId() != null) existing.setParentId(category.getParentId());
        if (category.getIcon() != null) existing.setIcon(category.getIcon());
        if (category.getSort() != null) existing.setSort(category.getSort());
        if (category.getStatus() != null) existing.setStatus(category.getStatus());
        existing.setUpdatedAt(LocalDateTime.now());

        categoryMapper.updateById(existing);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }
}
