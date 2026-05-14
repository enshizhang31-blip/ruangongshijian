package com.salemanager.modules.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.product.mapper.GoodsMapper;
import com.salemanager.modules.product.mapper.SkuMapper;
import com.salemanager.modules.product.model.Goods;
import com.salemanager.modules.product.model.Sku;
import com.salemanager.modules.product.param.SkuParam;
import com.salemanager.modules.product.service.SkuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SKU服务实现
 */
@Service
public class SkuServiceImpl implements SkuService {

    private static final Logger log = LoggerFactory.getLogger(SkuServiceImpl.class);

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public List<Sku> getSkuListBySpuId(Long spuId) {
        log.info("getSkuListBySpuId spuId={}", spuId);
        if (spuId == null || spuId <= 0) {
            throw new BusinessException(400, "商品ID无效");
        }

        return skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                .eq(Sku::getSpuId, spuId)
                .orderByDesc(Sku::getCreatedAt));
    }

    @Override
    @Transactional
    public void createSku(SkuParam param) {
        log.info("createSku spuId={}, skuCode={}", param.getSpuId(), param.getSkuCode());
        validateGoods(param.getSpuId());
        validateSkuCodeUnique(param.getSkuCode(), null);

        Sku sku = new Sku();
        copySkuParam(sku, param);
        sku.setStatus(param.getStatus() != null ? param.getStatus() : 1);
        sku.setCreatedAt(LocalDateTime.now());
        sku.setUpdatedAt(LocalDateTime.now());

        skuMapper.insert(sku);
        log.info("SKU创建成功 id={}, skuCode={}", sku.getId(), sku.getSkuCode());
    }

    @Override
    @Transactional
    public void updateSku(SkuParam param) {
        log.info("updateSku id={}", param.getId());
        if (param.getId() == null || param.getId() <= 0) {
            throw new BusinessException(400, "SKU ID无效");
        }

        Sku sku = skuMapper.selectById(param.getId());
        if (sku == null) {
            throw new BusinessException(404, "SKU不存在");
        }

        validateGoods(param.getSpuId());
        validateSkuCodeUnique(param.getSkuCode(), param.getId());

        copySkuParam(sku, param);
        sku.setUpdatedAt(LocalDateTime.now());
        skuMapper.updateById(sku);
        log.info("SKU更新成功 id={}", param.getId());
    }

    @Override
    @Transactional
    public void deleteSku(Long id) {
        log.info("deleteSku id={}", id);
        if (id == null || id <= 0) {
            throw new BusinessException(400, "SKU ID无效");
        }

        Sku sku = skuMapper.selectById(id);
        if (sku == null) {
            throw new BusinessException(404, "SKU不存在");
        }

        skuMapper.deleteById(id);
        log.info("SKU删除成功 id={}", id);
    }

    private void copySkuParam(Sku sku, SkuParam param) {
        sku.setSpuId(param.getSpuId());
        sku.setSkuCode(param.getSkuCode());
        sku.setSpecJson(StringUtils.hasText(param.getSpecJson()) ? param.getSpecJson() : null);
        sku.setPrice(param.getPrice());
        sku.setCostPrice(param.getCostPrice());
        sku.setUnit(StringUtils.hasText(param.getUnit()) ? param.getUnit() : null);
        sku.setImageUrl(StringUtils.hasText(param.getImageUrl()) ? param.getImageUrl() : null);
        sku.setStatus(param.getStatus());
    }

    private void validateGoods(Long spuId) {
        if (spuId == null || spuId <= 0) {
            throw new BusinessException(400, "商品ID无效");
        }

        Goods goods = goodsMapper.selectById(spuId);
        if (goods == null) {
            throw new BusinessException(404, "商品不存在");
        }
    }

    private void validateSkuCodeUnique(String skuCode, Long excludeId) {
        if (!StringUtils.hasText(skuCode)) {
            throw new BusinessException(400, "SKU编码不能为空");
        }

        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<Sku>()
                .eq(Sku::getSkuCode, skuCode);
        if (excludeId != null) {
            wrapper.ne(Sku::getId, excludeId);
        }

        Long count = skuMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException(400, "SKU编码已存在");
        }
    }
}