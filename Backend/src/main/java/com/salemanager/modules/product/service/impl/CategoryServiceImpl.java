package com.salemanager.modules.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.i18n.service.I18nSyncService;
import com.salemanager.modules.product.mapper.GoodsCategoryMapper;
import com.salemanager.modules.product.mapper.GoodsMapper;
import com.salemanager.modules.product.model.Goods;
import com.salemanager.modules.product.model.GoodsCategory;
import com.salemanager.modules.product.param.CategoryParam;
import com.salemanager.modules.product.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品分类服务实现
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private GoodsCategoryMapper categoryMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private I18nSyncService i18nSyncService;

    @Override
    public List<GoodsCategory> getCategoryList() {
        log.info("getCategoryList");
        LambdaQueryWrapper<GoodsCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(GoodsCategory::getSort);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    public GoodsCategory getCategoryById(Long id) {
        log.info("getCategoryById id={}", id);
        if (id == null || id <= 0) {
            throw new BusinessException(400, "分类ID无效");
        }

        GoodsCategory category = categoryMapper.selectById(id);
        if (category == null) {
            log.warn("分类不存在 id={}", id);
            throw new BusinessException("分类不存在");
        }
        return category;
    }

    @Override
    @Transactional
    public void createCategory(CategoryParam param) {
        log.info("createCategory name={}", param.getName());

        GoodsCategory category = new GoodsCategory();
        category.setName(param.getName());
        category.setParentId(param.getParentId());
        category.setIcon(param.getIcon());
        category.setSort(param.getSort() != null ? param.getSort() : 0);
        category.setStatus(param.getStatus() != null ? param.getStatus() : 1);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        categoryMapper.insert(category);
        i18nSyncService.syncCategoryCreated(category.getId(), category.getName(), null);
        log.info("分类创建成功 id={}", category.getId());
    }

    @Override
    @Transactional
    public void updateCategory(Long id, CategoryParam param) {
        log.info("updateCategory id={}", id);

        GoodsCategory existing = categoryMapper.selectById(id);
        if (existing == null) {
            log.warn("分类不存在 id={}", id);
            throw new BusinessException("分类不存在");
        }

        if (param.getName() != null) existing.setName(param.getName());
        if (param.getParentId() != null) existing.setParentId(param.getParentId());
        if (param.getIcon() != null) existing.setIcon(param.getIcon());
        if (param.getSort() != null) existing.setSort(param.getSort());
        if (param.getStatus() != null) existing.setStatus(param.getStatus());
        existing.setUpdatedAt(LocalDateTime.now());

        categoryMapper.updateById(existing);
        i18nSyncService.syncCategoryUpdated(existing.getId(), existing.getName(), null);
        log.info("分类更新成功 id={}", id);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.info("deleteCategory id={}", id);
        if (id == null || id <= 0) {
            throw new BusinessException(400, "分类ID无效");
        }

        GoodsCategory category = categoryMapper.selectById(id);
        if (category == null) {
            log.warn("分类不存在 id={}", id);
            throw new BusinessException("分类不存在");
        }

        // 检查分类下是否有SPU
        Long goodsCount = goodsMapper.selectCount(
                new LambdaQueryWrapper<Goods>().eq(Goods::getCategoryId, id));
        if (goodsCount > 0) {
            throw new BusinessException(
                    String.format("该分类下有 %d 个商品，无法删除", goodsCount));
        }

        categoryMapper.deleteById(id);
        i18nSyncService.syncCategoryDeleted(id);
        log.info("分类删除成功 id={}", id);
    }
}
