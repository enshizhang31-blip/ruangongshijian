package com.salemanager.modules.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.product.mapper.GoodsMapper;
import com.salemanager.modules.product.model.Goods;
import com.salemanager.modules.product.param.ProductParam;
import com.salemanager.modules.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品服务实现
 */
@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public List<Goods> getProductList(String keyword, Integer status, Integer page, Integer pageSize) {
        log.info("getProductList keyword={}, status={}, page={}, pageSize={}", keyword, status, page, pageSize);

        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Goods::getName, keyword)
                    .or()
                    .like(Goods::getBrand, keyword));
        }

        if (status != null) {
            wrapper.eq(Goods::getStatus, status);
        }

        wrapper.orderByDesc(Goods::getCreatedAt);

        // 使用MyBatis-Plus的分页插件，避免SQL注入风险
        IPage<Goods> result = new Page<>(page, pageSize);
        goodsMapper.selectPage(result, wrapper);

        return result.getRecords();
    }

    @Override
    public Long getProductCount(String keyword, Integer status) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Goods::getName, keyword)
                    .or()
                    .like(Goods::getBrand, keyword));
        }

        if (status != null) {
            wrapper.eq(Goods::getStatus, status);
        }

        return goodsMapper.selectCount(wrapper);
    }

    @Override
    public Goods getProductById(Long id) {
        log.info("getProductById id={}", id);
        if (id == null || id <= 0) {
            throw new BusinessException(400, "商品ID无效");
        }

        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            log.warn("商品不存在 id={}", id);
            throw new BusinessException("商品不存在");
        }
        return goods;
    }

    @Override
    @Transactional
    public void createProduct(ProductParam param) {
        log.info("createProduct name={}", param.getName());

        Goods goods = new Goods();
        goods.setName(param.getName());
        goods.setCategoryId(param.getCategoryId());
        goods.setBrand(param.getBrand());
        goods.setImageUrl(param.getImageUrl());
        goods.setImages(param.getImages());
        goods.setDescription(param.getDescription());
        goods.setStatus(param.getStatus() != null ? param.getStatus() : 0);
        goods.setCreatedAt(LocalDateTime.now());
        goods.setUpdatedAt(LocalDateTime.now());

        goodsMapper.insert(goods);
        log.info("商品创建成功 id={}", goods.getId());
    }

    @Override
    @Transactional
    public void updateProduct(Long id, ProductParam param) {
        log.info("updateProduct id={}", id);

        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            log.warn("商品不存在 id={}", id);
            throw new BusinessException("商品不存在");
        }

        if (StringUtils.hasText(param.getName())) {
            goods.setName(param.getName());
        }
        if (param.getCategoryId() != null) {
            goods.setCategoryId(param.getCategoryId());
        }
        if (param.getBrand() != null) {
            goods.setBrand(param.getBrand());
        }
        if (param.getImageUrl() != null) {
            goods.setImageUrl(param.getImageUrl());
        }
        if (param.getImages() != null) {
            goods.setImages(param.getImages());
        }
        if (param.getDescription() != null) {
            goods.setDescription(param.getDescription());
        }
        if (param.getStatus() != null) {
            goods.setStatus(param.getStatus());
        }
        goods.setUpdatedAt(LocalDateTime.now());

        goodsMapper.updateById(goods);
        log.info("商品更新成功 id={}", id);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("deleteProduct id={}", id);
        if (id == null || id <= 0) {
            throw new BusinessException(400, "商品ID无效");
        }

        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            log.warn("商品不存在 id={}", id);
            throw new BusinessException("商品不存在");
        }

        goodsMapper.deleteById(id);
        log.info("商品删除成功 id={}", id);
    }
}
