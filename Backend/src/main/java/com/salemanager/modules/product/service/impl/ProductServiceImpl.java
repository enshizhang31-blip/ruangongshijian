package com.salemanager.modules.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salemanager.modules.product.mapper.GoodsMapper;
import com.salemanager.modules.product.model.Goods;
import com.salemanager.modules.product.param.ProductParam;
import com.salemanager.modules.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品服务实现
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public List<Goods> getProductList(String keyword, Integer status, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(Goods::getName, keyword)
                    .or()
                    .like(Goods::getBrand, keyword));
        }

        if (status != null) {
            wrapper.eq(Goods::getStatus, status);
        }

        wrapper.orderByDesc(Goods::getCreatedAt);

        int offset = (page - 1) * pageSize;
        wrapper.last("LIMIT " + offset + ", " + pageSize);

        return goodsMapper.selectList(wrapper);
    }

    @Override
    public Long getProductCount(String keyword, Integer status) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
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
        return goodsMapper.selectById(id);
    }

    @Override
    public void createProduct(ProductParam param) {
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
    }

    @Override
    public void updateProduct(Long id, ProductParam param) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            return;
        }

        if (param.getName() != null) goods.setName(param.getName());
        if (param.getCategoryId() != null) goods.setCategoryId(param.getCategoryId());
        if (param.getBrand() != null) goods.setBrand(param.getBrand());
        if (param.getImageUrl() != null) goods.setImageUrl(param.getImageUrl());
        if (param.getImages() != null) goods.setImages(param.getImages());
        if (param.getDescription() != null) goods.setDescription(param.getDescription());
        if (param.getStatus() != null) goods.setStatus(param.getStatus());
        goods.setUpdatedAt(LocalDateTime.now());

        goodsMapper.updateById(goods);
    }

    @Override
    public void deleteProduct(Long id) {
        goodsMapper.deleteById(id);
    }
}
