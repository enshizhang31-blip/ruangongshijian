package com.salemanager.modules.i18n.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salemanager.modules.i18n.config.I18nProperties;
import com.salemanager.modules.i18n.model.TranslationUnit;
import com.salemanager.modules.i18n.model.TranslationUnit.LocaleEntry;
import com.salemanager.modules.i18n.repository.TranslationUnitRepository;
import com.salemanager.modules.product.mapper.GoodsCategoryMapper;
import com.salemanager.modules.product.mapper.GoodsMapper;
import com.salemanager.modules.product.mapper.SpecNameMapper;
import com.salemanager.modules.product.mapper.SpecValueMapper;
import com.salemanager.modules.product.model.Goods;
import com.salemanager.modules.product.model.GoodsCategory;
import com.salemanager.modules.product.model.SpecName;
import com.salemanager.modules.product.model.SpecValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 统一实体查询服务 — 从MySQL读取实体列表和字段，合并MongoDB翻译
 */
@Service
public class I18nEntityService {

    private static final Logger log = LoggerFactory.getLogger(I18nEntityService.class);

    private final GoodsMapper goodsMapper;
    private final GoodsCategoryMapper categoryMapper;
    private final SpecNameMapper specNameMapper;
    private final SpecValueMapper specValueMapper;
    private final TranslationUnitRepository translationRepo;
    private final I18nProperties properties;

    public I18nEntityService(GoodsMapper goodsMapper,
                             GoodsCategoryMapper categoryMapper,
                             SpecNameMapper specNameMapper,
                             SpecValueMapper specValueMapper,
                             TranslationUnitRepository translationRepo,
                             I18nProperties properties) {
        this.goodsMapper = goodsMapper;
        this.categoryMapper = categoryMapper;
        this.specNameMapper = specNameMapper;
        this.specValueMapper = specValueMapper;
        this.translationRepo = translationRepo;
        this.properties = properties;
    }

    /** 联合搜索实体列表 */
    public Map<String, Object> searchEntities(String entityType, String keyword, int page, int pageSize) {
        List<Map<String, Object>> items = new ArrayList<>();
        long total = 0;

        if (entityType == null || entityType.isEmpty() || "goods".equals(entityType)) {
            LambdaQueryWrapper<Goods> q = new LambdaQueryWrapper<Goods>()
                .select(Goods::getId, Goods::getName)
                .eq(Goods::getStatus, 1);
            if (keyword != null && !keyword.isEmpty()) {
                q.like(Goods::getName, keyword);
            }
            Page<Goods> pg = new Page<>(page + 1, pageSize);
            goodsMapper.selectPage(pg, q);
            for (Goods g : pg.getRecords()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("entityType", "goods");
                m.put("entityId", g.getId());
                m.put("name", g.getName());
                items.add(m);
            }
            total += pg.getTotal();
        }

        if (entityType == null || entityType.isEmpty() || "category".equals(entityType)) {
            LambdaQueryWrapper<GoodsCategory> q = new LambdaQueryWrapper<GoodsCategory>()
                .select(GoodsCategory::getId, GoodsCategory::getName);
            if (keyword != null && !keyword.isEmpty()) {
                q.like(GoodsCategory::getName, keyword);
            }
            Page<GoodsCategory> pg = new Page<>(page + 1, pageSize);
            categoryMapper.selectPage(pg, q);
            for (GoodsCategory c : pg.getRecords()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("entityType", "category");
                m.put("entityId", c.getId());
                m.put("name", c.getName());
                items.add(m);
            }
            total += pg.getTotal();
        }

        if (entityType == null || entityType.isEmpty() || "spec".equals(entityType)) {
            LambdaQueryWrapper<SpecName> q = new LambdaQueryWrapper<SpecName>()
                .select(SpecName::getId, SpecName::getName);
            if (keyword != null && !keyword.isEmpty()) {
                q.like(SpecName::getName, keyword);
            }
            Page<SpecName> pg = new Page<>(page + 1, pageSize);
            specNameMapper.selectPage(pg, q);
            for (SpecName s : pg.getRecords()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("entityType", "spec");
                m.put("entityId", s.getId());
                m.put("name", s.getName());
                items.add(m);
            }
            total += pg.getTotal();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    /** 获取实体翻译字段（MySQL数据 + MongoDB翻译合并） */
    public Map<String, Object> getEntityFields(String entityType, Long entityId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("entityType", entityType);
        result.put("entityId", entityId);

        List<String> supportedLocales = properties.getSupportedLocales();

        Map<String, TranslationUnit> existingUnits = new LinkedHashMap<>();
        for (TranslationUnit u : translationRepo.findByEntityTypeAndEntityIdOrderBySortOrderAsc(entityType, entityId)) {
            existingUnits.put(u.getFieldPath(), u);
        }

        List<Map<String, Object>> fields = new ArrayList<>();

        switch (entityType) {
            case "goods" -> {
                Goods goods = goodsMapper.selectById(entityId);
                if (goods == null) throw new RuntimeException("商品不存在");
                result.put("entityName", goods.getName());
                fields.add(buildField(existingUnits, "goods", entityId, "name", "text",
                    "商品名称", "商品正式名称，用于列表和详情页标题", 0, goods.getName(), supportedLocales));
                if (goods.getShortDesc() != null) {
                    fields.add(buildField(existingUnits, "goods", entityId, "short_desc", "text",
                        "短描述", "简短描述，用于卡片展示", 1, goods.getShortDesc(), supportedLocales));
                }
                if (goods.getDescription() != null) {
                    fields.add(buildField(existingUnits, "goods", entityId, "description", "rich_text",
                        "商品描述", "详细描述，支持富文本", 2, goods.getDescription(), supportedLocales));
                }
            }
            case "category" -> {
                GoodsCategory cat = categoryMapper.selectById(entityId);
                if (cat == null) throw new RuntimeException("分类不存在");
                result.put("entityName", cat.getName());
                fields.add(buildField(existingUnits, "category", entityId, "name", "text",
                    "分类名称", "分类显示名称", 0, cat.getName(), supportedLocales));
            }
            case "spec" -> {
                SpecName spec = specNameMapper.selectById(entityId);
                if (spec == null) throw new RuntimeException("规格不存在");
                result.put("entityName", spec.getName());
                fields.add(buildField(existingUnits, "spec", entityId, "name", "text",
                    "规格名称", "规格名称", 0, spec.getName(), supportedLocales));

                List<SpecValue> values = specValueMapper.selectList(
                    new LambdaQueryWrapper<SpecValue>().eq(SpecValue::getSpecId, entityId).orderByAsc(SpecValue::getSort));
                int idx = 1;
                for (SpecValue sv : values) {
                    fields.add(buildField(existingUnits, "spec", entityId, "value_" + sv.getId(), "text",
                        "规格值-" + sv.getValue(), "规格可选值", idx++, sv.getValue(), supportedLocales));
                }
            }
        }

        result.put("fields", fields);
        return result;
    }

    private Map<String, Object> buildField(Map<String, TranslationUnit> existing,
                                           String entityType, Long entityId,
                                           String fieldPath, String fieldType,
                                           String name, String description,
                                           int sortOrder, Object baseValue,
                                           List<String> supportedLocales) {
        TranslationUnit existingUnit = existing.get(fieldPath);
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("fieldPath", fieldPath);
        field.put("fieldType", fieldType);
        field.put("name", name);
        field.put("description", description);
        field.put("sortOrder", sortOrder);
        field.put("unitKey", entityType + ":" + entityId + ":" + fieldPath);
        field.put("hasMongo", existingUnit != null);

        Map<String, Object> locales = new LinkedHashMap<>();
        for (String loc : supportedLocales) {
            Map<String, Object> entry = new LinkedHashMap<>();
            if (existingUnit != null) {
                LocaleEntry le = existingUnit.getLocales().get(loc);
                entry.put("value", le != null ? le.getValue() : null);
                entry.put("status", le != null ? le.getStatus() : "draft");
            } else {
                entry.put("value", loc.equals(properties.getDefaultLocale()) ? baseValue : null);
                entry.put("status", loc.equals(properties.getDefaultLocale()) ? "approved" : "draft");
            }
            locales.put(loc, entry);
        }
        field.put("locales", locales);
        return field;
    }
}
