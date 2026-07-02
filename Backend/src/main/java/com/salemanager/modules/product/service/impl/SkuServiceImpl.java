package com.salemanager.modules.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.product.mapper.GoodsMapper;
import com.salemanager.modules.product.mapper.SkuMapper;
import com.salemanager.modules.product.mapper.SpecNameMapper;
import com.salemanager.modules.product.mapper.SpecValueMapper;
import com.salemanager.modules.product.model.Goods;
import com.salemanager.modules.product.model.Sku;
import com.salemanager.modules.product.model.SpecName;
import com.salemanager.modules.product.model.SpecValue;
import com.salemanager.modules.product.param.BatchGenerateSkuParam;
import com.salemanager.modules.product.param.SkuParam;
import com.salemanager.modules.product.service.SkuService;
import com.salemanager.modules.sn.mapper.SnCodeMapper;
import com.salemanager.modules.sn.model.SnCode;
import com.salemanager.modules.i18n.service.I18nSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private SpecValueMapper specValueMapper;

    @Autowired
    private SpecNameMapper specNameMapper;

    @Autowired
    private SnCodeMapper snCodeMapper;

    @Autowired
    private I18nSyncService i18nSyncService;

    @Override
    public List<Sku> getSkuListBySpuId(Long spuId) {
        log.info("getSkuListBySpuId spuId={}", spuId);
        if (spuId == null || spuId <= 0) {
            throw new BusinessException(400, "商品ID无效");
        }

        List<Sku> skus = skuMapper.selectList(new LambdaQueryWrapper<Sku>()
                .eq(Sku::getSpuId, spuId)
                .orderByDesc(Sku::getCreatedAt));

        // 填充库存
        if (!skus.isEmpty()) {
            enrichWithStock(skus);
        }
        return skus;
    }

    private void enrichWithStock(List<Sku> skus) {
        List<Long> skuIds = skus.stream().map(Sku::getId).collect(Collectors.toList());
        List<SnCode> inStockSns = snCodeMapper.selectList(
                new LambdaQueryWrapper<SnCode>()
                        .in(SnCode::getSkuId, skuIds)
                        .eq(SnCode::getStatus, 0));
        Map<Long, Long> stockMap = inStockSns.stream()
                .collect(Collectors.groupingBy(SnCode::getSkuId, Collectors.counting()));
        for (Sku sku : skus) {
            sku.setStock(stockMap.getOrDefault(sku.getId(), 0L).intValue());
        }
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

        // 同步 SKU 翻译 (类似 SPU 的多语言支持)
        Goods spu = goodsMapper.selectById(sku.getSpuId());
        i18nSyncService.syncSkuCreated(
                sku.getId(),
                sku.getSkuCode(),
                sku.getSpecJson(),
                sku.getUnit(),
                spu != null ? spu.getName() : null
        );

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

        // 同步 SKU 翻译
        Goods spu = goodsMapper.selectById(sku.getSpuId());
        i18nSyncService.syncSkuUpdated(
                sku.getId(),
                sku.getSpecJson(),
                sku.getUnit(),
                spu != null ? spu.getName() : null
        );

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

        // 安全检查：是否有在库SN码
        Long inStockCount = snCodeMapper.selectCount(
                new LambdaQueryWrapper<SnCode>()
                        .eq(SnCode::getSkuId, id)
                        .eq(SnCode::getStatus, 0));
        if (inStockCount > 0) {
            throw new BusinessException(
                    String.format("该SKU下有 %d 个在库SN码，请先处理后再删除", inStockCount));
        }

        // 级联删除所有SN码和操作日志
        List<SnCode> snCodes = snCodeMapper.selectList(
                new LambdaQueryWrapper<SnCode>().eq(SnCode::getSkuId, id));
        if (!snCodes.isEmpty()) {
            List<Long> snCodeIds = snCodes.stream().map(SnCode::getId).collect(Collectors.toList());
            // 删除操作日志
            snCodeMapper.delete(new LambdaQueryWrapper<SnCode>().eq(SnCode::getSkuId, id));
            log.info("级联删除SN码 count={}", snCodes.size());
        }

        skuMapper.deleteById(id);

        // 同步 SKU 翻译清理
        i18nSyncService.syncSkuDeleted(id);

        log.info("SKU删除成功 id={}", id);
    }

    @Override
    @Transactional
    public List<Sku> batchGenerateSkus(BatchGenerateSkuParam param) {
        log.info("batchGenerateSkus spuId={}, specIds={}", param.getSpuId(), param.getSpecIds());
        validateGoods(param.getSpuId());

        // 1. 获取每个规格的所有值
        List<List<SpecValue>> specValuesList = new ArrayList<>();

        // 加载规格名称映射
        List<SpecName> specNames = specNameMapper.selectBatchIds(param.getSpecIds());
        Map<Long, String> specNameMap = specNames.stream()
                .collect(Collectors.toMap(SpecName::getId, SpecName::getName));

        for (Long specId : param.getSpecIds()) {
            List<SpecValue> values = specValueMapper.selectList(
                    new LambdaQueryWrapper<SpecValue>()
                            .eq(SpecValue::getSpecId, specId)
                            .orderByAsc(SpecValue::getSort));
            if (values.isEmpty()) {
                throw new BusinessException(String.format("规格ID %d 下没有规格值", specId));
            }
            // 填充规格名称
            String specName = specNameMap.getOrDefault(specId, "规格" + specId);
            values.forEach(v -> v.setSpecName(specName));
            specValuesList.add(values);
        }

        // 2. 笛卡尔积
        List<List<SpecValue>> combinations = cartesianProduct(specValuesList);
        log.info("生成 {} 个规格组合", combinations.size());

        // 3. 为每个组合创建SKU
        List<Sku> skus = new ArrayList<>();
        Goods goods = goodsMapper.selectById(param.getSpuId());
        String spuAbbr = abbreviate(goods != null ? goods.getName() : "");
        String prefix = StringUtils.hasText(param.getCodePrefix()) ? param.getCodePrefix() : "SKU";
        String spuTag = prefix + "-" + spuAbbr + "-";
        BigDecimal price = param.getDefaultPrice() != null ? param.getDefaultPrice() : BigDecimal.ZERO;
        BigDecimal costPrice = param.getDefaultCostPrice() != null ? param.getDefaultCostPrice() : BigDecimal.ZERO;
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < combinations.size(); i++) {
            List<SpecValue> combo = combinations.get(i);
            Sku sku = new Sku();
            sku.setSpuId(param.getSpuId());
            sku.setSkuCode(spuTag + (i + 1) + "-SPU" + param.getSpuId());
            sku.setSpecJson(buildSpecJson(combo));
            sku.setPrice(price);
            sku.setCostPrice(costPrice);
            sku.setStatus(1);
            sku.setCreatedAt(now);
            sku.setUpdatedAt(now);
            skus.add(sku);
        }

        // 批量插入
        for (Sku sku : skus) {
            // 检查skuCode唯一性
            Long exists = skuMapper.selectCount(
                    new LambdaQueryWrapper<Sku>().eq(Sku::getSkuCode, sku.getSkuCode()));
            if (exists > 0) {
                log.warn("SKU编码已存在，跳过: {}", sku.getSkuCode());
                continue;
            }
            skuMapper.insert(sku);
            // 同步 SKU 翻译
            i18nSyncService.syncSkuCreated(
                    sku.getId(),
                    sku.getSkuCode(),
                    sku.getSpecJson(),
                    sku.getUnit(),
                    goods != null ? goods.getName() : null
            );
        }

        log.info("批量生成SKU完成，共创建 {} 个", skus.size());
        return skus;
    }

    /** 笛卡尔积算法 */
    private List<List<SpecValue>> cartesianProduct(List<List<SpecValue>> lists) {
        List<List<SpecValue>> result = new ArrayList<>();
        result.add(new ArrayList<>());
        for (List<SpecValue> list : lists) {
            List<List<SpecValue>> temp = new ArrayList<>();
            for (List<SpecValue> r : result) {
                for (SpecValue v : list) {
                    List<SpecValue> nr = new ArrayList<>(r);
                    nr.add(v);
                    temp.add(nr);
                }
            }
            result = temp;
        }
        return result;
    }


    /** 简单缩写：取前4个有效字符（过滤中文） */
    private String abbreviate(String value) {
        if (value == null || value.isEmpty()) return "XX";
        String cleaned = value.replaceAll("[\\u4e00-\\u9fa5]", "").replaceAll("[^A-Za-z0-9]", "");
        if (cleaned.isEmpty()) return "V" + Math.abs(value.hashCode() % 1000);
        return cleaned.substring(0, Math.min(4, cleaned.length())).toUpperCase();
    }

    /** 构建specJson */
    private String buildSpecJson(List<SpecValue> combo) {
        // 需要根据specValue的specId反查specName
        StringBuilder json = new StringBuilder("{");
        for (int i = 0; i < combo.size(); i++) {
            SpecValue sv = combo.get(i);
            if (i > 0) json.append(",");
            json.append("\"").append(escapeJson(sv.getSpecName())).append("\":\"")
                    .append(escapeJson(sv.getValue())).append("\"");
        }
        json.append("}");
        return json.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
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