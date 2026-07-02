package com.salemanager.modules.i18n.service;

import com.salemanager.modules.i18n.model.TranslationUnit;
import com.salemanager.modules.i18n.repository.TranslationUnitRepository;
import com.salemanager.modules.i18n.model.TranslationUnit.LocaleEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 翻译数据同步服务 — 在实体CRUD时自动维护 translation_units
 */
@Service
public class I18nSyncService {

    private static final Logger log = LoggerFactory.getLogger(I18nSyncService.class);
    private static final List<String> DEFAULT_LOCALES = List.of("zh-CN", "en-US", "ja-JP");

    private final TranslationUnitRepository repo;
    private final I18nCacheService cache;

    public I18nSyncService(TranslationUnitRepository repo, I18nCacheService cache) {
        this.repo = repo;
        this.cache = cache;
    }

    // ==================== 商品 ====================

    public void syncGoodsCreated(Long goodsId, String name, String shortDesc, String description) {
        int order = 0;
        upsertUnit("goods", goodsId, "name", "text", "商品名称", "商品正式名称", name, order++);
        if (shortDesc != null) {
            upsertUnit("goods", goodsId, "short_desc", "text", "短描述", "简短描述", shortDesc, order++);
        }
        if (description != null) {
            upsertUnit("goods", goodsId, "description", "rich_text", "商品描述", "详细描述", description, order++);
        }
        cache.evictEntity("goods", goodsId);
    }

    public void syncGoodsUpdated(Long goodsId, String name, String shortDesc, String description) {
        updateBaseValue("goods", goodsId, "name", name);
        if (shortDesc != null) updateBaseValue("goods", goodsId, "short_desc", shortDesc);
        if (description != null) updateBaseValue("goods", goodsId, "description", description);
        cache.evictEntity("goods", goodsId);
    }

    public void syncGoodsDeleted(Long goodsId) {
        repo.deleteByEntityTypeAndEntityId("goods", goodsId);
        cache.evictEntity("goods", goodsId);
        log.info("syncGoodsDeleted goodsId={}", goodsId);
    }

    // ==================== SKU (类似 SPU 的多语言支持) ====================

    /**
     * SKU 创建时同步翻译单元
     * @param skuId SKU ID
     * @param skuCode SKU 编码 (作为唯一标识, 通常不需要翻译)
     * @param specJson 规格组合 JSON, 例如 {"颜色":"红色","尺码":"XL"}
     * @param unit 计量单位 (例如: 件, kg, 米)
     * @param spuName 所属 SPU 名称 (作为 SKU 显示名默认值)
     */
    public void syncSkuCreated(Long skuId, String skuCode, String specJson, String unit, String spuName) {
        // SKU 显示名: 默认用 SPU 名 + 规格
        String skuDisplayName = buildSkuDisplayName(spuName, specJson);
        upsertUnit("sku", skuId, "name", "text", "SKU名称", "SKU显示名称", skuDisplayName, 0);
        // 规格描述: 直接存储 specJson
        if (specJson != null && !specJson.trim().isEmpty()) {
            upsertUnit("sku", skuId, "spec", "text", "规格", "规格组合", specJson, 1);
        }
        // 单位
        if (unit != null && !unit.trim().isEmpty()) {
            upsertUnit("sku", skuId, "unit", "text", "单位", "计量单位", unit, 2);
        }
        cache.evictEntity("sku", skuId);
    }

    /**
     * SKU 更新时同步翻译单元
     */
    public void syncSkuUpdated(Long skuId, String specJson, String unit, String spuName) {
        if (specJson != null) {
            String skuDisplayName = buildSkuDisplayName(spuName, specJson);
            // SKU name 字段同步更新
            updateBaseValue("sku", skuId, "name", skuDisplayName);
            updateBaseValue("sku", skuId, "spec", specJson);
        }
        if (unit != null) {
            updateBaseValue("sku", skuId, "unit", unit);
        }
        cache.evictEntity("sku", skuId);
    }

    /**
     * SKU 删除时清理翻译单元
     */
    public void syncSkuDeleted(Long skuId) {
        repo.deleteByEntityTypeAndEntityId("sku", skuId);
        cache.evictEntity("sku", skuId);
        log.info("syncSkuDeleted skuId={}", skuId);
    }

    /**
     * 构建 SKU 显示名: SPU名 + 规格组合
     */
    private String buildSkuDisplayName(String spuName, String specJson) {
        StringBuilder sb = new StringBuilder();
        if (spuName != null && !spuName.isEmpty()) {
            sb.append(spuName);
        }
        if (specJson != null && !specJson.trim().isEmpty()) {
            // 简单解析 {"颜色":"红色","尺码":"XL"} → 红色/XL
            String spec = specJson.replaceAll("[{}\"\\s]", "")
                                  .replace(",", "/")
                                  .replace(":", "/");
            if (sb.length() > 0) sb.append(" ");
            sb.append(spec);
        }
        return sb.toString();
    }

    // ==================== 分类 ====================

    public void syncCategoryCreated(Long categoryId, String name, String description) {
        int order = 0;
        upsertUnit("category", categoryId, "name", "text", "分类名称", "分类显示名称", name, order++);
        if (description != null) {
            upsertUnit("category", categoryId, "description", "text", "分类描述", "分类说明", description, order++);
        }
        cache.evictEntity("category", categoryId);
    }

    public void syncCategoryUpdated(Long categoryId, String name, String description) {
        updateBaseValue("category", categoryId, "name", name);
        if (description != null) updateBaseValue("category", categoryId, "description", description);
        cache.evictEntity("category", categoryId);
    }

    public void syncCategoryDeleted(Long categoryId) {
        repo.deleteByEntityTypeAndEntityId("category", categoryId);
        cache.evictEntity("category", categoryId);
    }

    // ==================== 规格 ====================

    public void syncSpecCreated(Long specId, String name) {
        // 先查是否已有
        if (repo.findByUnitKey("spec:" + specId + ":name") != null) return;
        upsertUnit("spec", specId, "name", "text", "规格名称", "规格名称", name, 0);
    }

    public void syncSpecUpdated(Long specId, String name) {
        updateBaseValue("spec", specId, "name", name);
        cache.evictEntity("spec", specId);
    }

    public void syncSpecValueCreated(Long specId, Long valueId, String value) {
        upsertUnit("spec", specId, "value_" + valueId, "text", "规格值-" + value, "规格值", value, valueId.intValue());
        cache.evictEntity("spec", specId);
    }

    public void syncSpecValueUpdated(Long specId, Long valueId, String value) {
        updateBaseValue("spec", specId, "value_" + valueId, value);
        cache.evictEntity("spec", specId);
    }

    public void syncSpecDeleted(Long specId) {
        repo.deleteByEntityTypeAndEntityId("spec", specId);
        cache.evictEntity("spec", specId);
    }

    public void syncSpecValueDeleted(Long specId, Long valueId) {
        TranslationUnit unit = repo.findByUnitKey("spec:" + specId + ":value_" + valueId);
        if (unit != null) {
            repo.delete(unit);
            cache.evictEntity("spec", specId);
        }
    }

    // ==================== 内部方法 ====================

    private void upsertUnit(String entityType, Long entityId, String fieldPath,
                            String fieldType, String name, String desc, Object zhValue, int sortOrder) {
        String unitKey = entityType + ":" + entityId + ":" + fieldPath;
        TranslationUnit unit = repo.findByUnitKey(unitKey);
        if (unit == null) {
            unit = new TranslationUnit();
            unit.setUnitKey(unitKey);
            unit.setEntityType(entityType);
            unit.setEntityId(entityId);
            unit.setFieldPath(fieldPath);
            unit.setName(name);
            unit.setDescription(desc);
            unit.setFieldType(fieldType);
            unit.setSortOrder(sortOrder);
            unit.setBaseLocale("zh-CN");
            unit.setLocales(new java.util.LinkedHashMap<>());
            unit.setCreatedAt(LocalDateTime.now());
        } else {
            unit.setName(name);
            unit.setDescription(desc);
        }

        unit.getLocales().put("zh-CN", new LocaleEntry(zhValue, "approved"));
        for (String loc : DEFAULT_LOCALES) {
            unit.getLocales().putIfAbsent(loc, new LocaleEntry(null, "draft"));
        }
        unit.setUpdatedAt(LocalDateTime.now());
        try {
            repo.save(unit);
        } catch (DuplicateKeyException e) {
            // 兜底：唯一索引冲突时重新查找并更新
            TranslationUnit existing = repo.findByUnitKey(unitKey);
            if (existing != null) {
                existing.setName(name);
                existing.setDescription(desc);
                existing.setFieldType(fieldType);
                existing.getLocales().put("zh-CN", new LocaleEntry(zhValue, "approved"));
                for (String loc : DEFAULT_LOCALES) {
                    existing.getLocales().putIfAbsent(loc, new LocaleEntry(null, "draft"));
                }
                existing.setUpdatedAt(LocalDateTime.now());
                repo.save(existing);
            } else {
                log.warn("upsertUnit DuplicateKey但查询不到已有记录 unitKey={}", unitKey);
            }
        }
    }

    private void updateBaseValue(String entityType, Long entityId, String fieldPath, Object newValue) {
        String unitKey = entityType + ":" + entityId + ":" + fieldPath;
        TranslationUnit unit = repo.findByUnitKey(unitKey);
        if (unit == null) return;
        unit.getLocales().computeIfAbsent("zh-CN", k -> new LocaleEntry(null, "approved"));
        unit.getLocales().get("zh-CN").setValue(newValue);
        unit.getLocales().get("zh-CN").setUpdatedAt(LocalDateTime.now());
        unit.setUpdatedAt(LocalDateTime.now());
        repo.save(unit);
    }
}
