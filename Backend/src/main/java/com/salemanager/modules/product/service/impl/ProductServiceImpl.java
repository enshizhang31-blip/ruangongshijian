package com.salemanager.modules.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.product.mapper.GoodsMapper;
import com.salemanager.modules.product.mapper.SkuMapper;
import com.salemanager.modules.product.model.Goods;
import com.salemanager.modules.product.model.Sku;
import com.salemanager.modules.product.param.ProductParam;
import com.salemanager.modules.product.service.ProductService;
import com.salemanager.modules.i18n.service.I18nSyncService;
import com.salemanager.modules.sn.mapper.SnCodeLogMapper;
import com.salemanager.modules.sn.mapper.SnCodeMapper;
import com.salemanager.modules.sn.model.SnCode;
import com.salemanager.modules.sn.model.SnCodeLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品服务实现
 */
@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SnCodeMapper snCodeMapper;

    @Autowired
    private SnCodeLogMapper snCodeLogMapper;

    @Autowired
    private I18nSyncService i18nSyncService;

    @Override
    public List<Goods> getProductList(String keyword, Long categoryId, Integer status, Integer page, Integer pageSize) {
        LambdaQueryWrapper<Goods> wrapper = buildListWrapper(keyword, categoryId, status);
        wrapper.orderByDesc(Goods::getCreatedAt);

        IPage<Goods> result = new Page<>(page, pageSize);
        goodsMapper.selectPage(result, wrapper);

        List<Goods> list = result.getRecords();
        // 填充SKU数和库存
        if (!list.isEmpty()) {
            enrichWithSkuStats(list);
        }
        return list;
    }

    @Override
    public Long getProductCount(String keyword, Long categoryId, Integer status) {
        LambdaQueryWrapper<Goods> wrapper = buildListWrapper(keyword, categoryId, status);
        return goodsMapper.selectCount(wrapper);
    }

    private LambdaQueryWrapper<Goods> buildListWrapper(String keyword, Long categoryId, Integer status) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Goods::getName, keyword)
                    .or()
                    .like(Goods::getBrand, keyword));
        }
        if (categoryId != null) {
            wrapper.eq(Goods::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Goods::getStatus, status);
        }
        return wrapper;
    }

    private void enrichWithSkuStats(List<Goods> goodsList) {
        List<Long> spuIds = goodsList.stream().map(Goods::getId).collect(Collectors.toList());

        // 查询所有关联SKU
        List<Sku> allSkus = skuMapper.selectList(
                new LambdaQueryWrapper<Sku>().in(Sku::getSpuId, spuIds));
        Map<Long, List<Sku>> skuMap = allSkus.stream()
                .collect(Collectors.groupingBy(Sku::getSpuId));

        // 查询所有在库SN码
        List<SnCode> inStockSns = snCodeMapper.selectList(
                new LambdaQueryWrapper<SnCode>()
                        .in(SnCode::getSpuId, spuIds)
                        .eq(SnCode::getStatus, 0));
        Map<Long, Long> stockMap = inStockSns.stream()
                .collect(Collectors.groupingBy(SnCode::getSpuId, Collectors.counting()));

        for (Goods g : goodsList) {
            List<Sku> skus = skuMap.getOrDefault(g.getId(), Collections.emptyList());
            g.setSkuCount(skus.size());
            g.setStockCount(stockMap.getOrDefault(g.getId(), 0L).intValue());
        }
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
        // 填充SKU数 + 库存总数 + 销量
        List<Sku> skus = skuMapper.selectList(new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, id));
        goods.setSkuCount(skus.size());
        int stock = 0;
        for (Sku s : skus) {
            if (s.getStock() != null) stock += s.getStock();
        }
        goods.setStockCount(stock);

        // 销量：SN 状态 2/3/4 (已售/已发货/已签收) 都算销售
        try {
            java.util.List<Long> skuIds = new java.util.ArrayList<>();
            for (Sku s : skus) skuIds.add(s.getId());
            int sales = 0;
            if (!skuIds.isEmpty()) {
                Long c = snCodeMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.salemanager.modules.sn.model.SnCode>()
                        .in(com.salemanager.modules.sn.model.SnCode::getSkuId, skuIds)
                        .in(com.salemanager.modules.sn.model.SnCode::getStatus, java.util.Arrays.asList(2, 3, 4)));
                sales = c == null ? 0 : c.intValue();
            }
            goods.setSalesCount(sales);
        } catch (Exception e) {
            log.warn("计算销量失败: {}", e.getMessage());
            goods.setSalesCount(0);
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
        goods.setShortDesc(param.getShortDesc());
        goods.setDescription(param.getDescription());
        goods.setStatus(param.getStatus() != null ? param.getStatus() : 0);
        goods.setCreatedAt(LocalDateTime.now());
        goods.setUpdatedAt(LocalDateTime.now());

        goodsMapper.insert(goods);

        // 同步 SPU 翻译 (类似 SKU 的多语言支持)
        i18nSyncService.syncGoodsCreated(
                goods.getId(),
                goods.getName(),
                goods.getShortDesc(),
                goods.getDescription()
        );

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
        if (param.getShortDesc() != null) {
            goods.setShortDesc(param.getShortDesc());
        }
        if (param.getDescription() != null) {
            goods.setDescription(param.getDescription());
        }
        if (param.getStatus() != null) {
            goods.setStatus(param.getStatus());
        }
        goods.setUpdatedAt(LocalDateTime.now());

        goodsMapper.updateById(goods);

        // 同步 SPU 翻译
        i18nSyncService.syncGoodsUpdated(
                goods.getId(),
                goods.getName(),
                goods.getShortDesc(),
                goods.getDescription()
        );

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

        // 级联删除：SN码日志 → SN码 → SKU → SPU
        List<Sku> skus = skuMapper.selectList(new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, id));
        List<Long> skuIds = skus.stream().map(Sku::getId).collect(Collectors.toList());

        if (!skuIds.isEmpty()) {
            List<SnCode> snCodes = snCodeMapper.selectList(
                    new LambdaQueryWrapper<SnCode>().in(SnCode::getSkuId, skuIds));
            List<Long> snCodeIds = snCodes.stream().map(SnCode::getId).collect(Collectors.toList());

            if (!snCodeIds.isEmpty()) {
                // 删除SN码操作日志
                snCodeLogMapper.delete(new LambdaQueryWrapper<SnCodeLog>().in(
                        SnCodeLog::getSnCodeId, snCodeIds));
                log.info("删除SN码日志 count={}", snCodeIds.size());
                // 删除SN码
                snCodeMapper.deleteBatchIds(snCodeIds);
                log.info("删除SN码 count={}", snCodeIds.size());
            }
            // 删除SKU
            skuMapper.delete(new LambdaQueryWrapper<Sku>().eq(Sku::getSpuId, id));
            log.info("删除SKU count={}", skus.size());
        }

        goodsMapper.deleteById(id);

        // 同步 SPU 翻译清理
        i18nSyncService.syncGoodsDeleted(id);

        log.info("商品删除成功 id={}", id);
    }

    @Override
    @Transactional
    public void updateProductStatus(Long id, Integer status) {
        log.info("updateProductStatus id={}, status={}", id, status);
        if (id == null || id <= 0) {
            throw new BusinessException(400, "商品ID无效");
        }

        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }

        // 上架校验：必须有至少1个启用且库存>0的SKU
        if (status != null && status == 1) {
            List<Sku> enabledSkus = skuMapper.selectList(
                    new LambdaQueryWrapper<Sku>()
                            .eq(Sku::getSpuId, id)
                            .eq(Sku::getStatus, 1));
            if (enabledSkus.isEmpty()) {
                throw new BusinessException("上架失败：该商品没有启用的SKU");
            }

            List<Long> skuIds = enabledSkus.stream().map(Sku::getId).collect(Collectors.toList());
            Long inStockCount = snCodeMapper.selectCount(
                    new LambdaQueryWrapper<SnCode>()
                            .in(SnCode::getSkuId, skuIds)
                            .eq(SnCode::getStatus, 0));
            if (inStockCount == 0) {
                throw new BusinessException("上架失败：该商品所有SKU库存为0");
            }
        }

        goods.setStatus(status);
        goods.setUpdatedAt(LocalDateTime.now());
        goodsMapper.updateById(goods);
        log.info("商品状态更新成功 id={}, status={}", id, status);
    }
}
