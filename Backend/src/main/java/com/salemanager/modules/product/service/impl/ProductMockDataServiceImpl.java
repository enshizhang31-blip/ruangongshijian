package com.salemanager.modules.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.product.mapper.GoodsCategoryMapper;
import com.salemanager.modules.product.mapper.GoodsMapper;
import com.salemanager.modules.product.mapper.SkuMapper;
import com.salemanager.modules.product.mapper.SpecNameMapper;
import com.salemanager.modules.product.mapper.SpecValueMapper;
import com.salemanager.modules.product.model.Goods;
import com.salemanager.modules.product.model.GoodsCategory;
import com.salemanager.modules.product.model.Sku;
import com.salemanager.modules.product.model.SpecName;
import com.salemanager.modules.product.model.SpecValue;
import com.salemanager.modules.product.param.ProductMockDataParam;
import com.salemanager.modules.product.service.ProductMockDataService;
import com.salemanager.modules.sn.mapper.SnCodeMapper;
import com.salemanager.modules.sn.model.SnCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 商品模拟数据服务实现
 */
@Service
public class ProductMockDataServiceImpl implements ProductMockDataService {

    private static final Logger log = LoggerFactory.getLogger(ProductMockDataServiceImpl.class);
    private static final DateTimeFormatter BATCH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final List<String> CATEGORY_NAMES = Arrays.asList("数码产品", "办公用品", "智能家居", "生活电器", "配件周边");
    private static final List<SpecTemplate> SPEC_TEMPLATES = Arrays.asList(
            new SpecTemplate("颜色", Arrays.asList("黑色", "白色", "蓝色", "红色", "绿色", "金色", "银色")),
            new SpecTemplate("容量", Arrays.asList("64G", "128G", "256G", "512G", "1TB")),
            new SpecTemplate("版本", Arrays.asList("标准版", "Pro版", "Max版", "Ultra版")),
            new SpecTemplate("尺寸", Arrays.asList("S", "M", "L", "XL", "XXL")),
            new SpecTemplate("套餐", Arrays.asList("单机", "标配", "豪华版", "旗舰版"))
    );
    private static final List<String> BRAND_NAMES = Arrays.asList("Apple", "HUAWEI", "Xiaomi", "OPPO", "vivo", "Samsung", "Dell", "Lenovo");

    @Autowired
    private GoodsCategoryMapper goodsCategoryMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SpecNameMapper specNameMapper;

    @Autowired
    private SpecValueMapper specValueMapper;

    @Autowired
    private SnCodeMapper snCodeMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public Map<String, Object> generateMockData(ProductMockDataParam param) {
        List<GoodsCategory> categories = ensureCategories();
        List<SpecName> specs = ensureSpecs(param.getSpecCount(), param.getValuesPerSpec());
        String batchCode = buildBatchCode();

        AtomicInteger goodsCount = new AtomicInteger();
        AtomicInteger skuCount = new AtomicInteger();
        AtomicInteger snCount = new AtomicInteger();

        for (int i = 0; i < param.getGoodsCount(); i++) {
            GoodsCategory category = categories.get(i % categories.size());
            Goods goods = buildGoods(batchCode, i + 1, category);
            goodsMapper.insert(goods);
            goodsCount.incrementAndGet();

            createSkusForGoods(goods, batchCode, i + 1, param, specs, skuCount, snCount);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("batchCode", batchCode);
        result.put("goodsCount", goodsCount.get());
        result.put("skuCount", skuCount.get());
        result.put("snCount", snCount.get());
        result.put("categoryCount", categories.size());
        result.put("specCount", specs.size());
        return result;
    }

    private List<GoodsCategory> ensureCategories() {
        List<GoodsCategory> categories = goodsCategoryMapper.selectList(new LambdaQueryWrapper<GoodsCategory>().orderByAsc(GoodsCategory::getSort).orderByAsc(GoodsCategory::getId));
        if (!categories.isEmpty()) {
            return categories;
        }

        List<GoodsCategory> created = new ArrayList<>();
        for (int i = 0; i < CATEGORY_NAMES.size(); i++) {
            GoodsCategory category = new GoodsCategory();
            category.setName(CATEGORY_NAMES.get(i));
            category.setParentId(0L);
            category.setSort(i + 1);
            category.setStatus(1);
            category.setCreatedAt(LocalDateTime.now());
            category.setUpdatedAt(LocalDateTime.now());
            goodsCategoryMapper.insert(category);
            created.add(category);
        }
        return created;
    }

    private List<SpecName> ensureSpecs(int specCount, int valuesPerSpec) {
        List<SpecName> specs = specNameMapper.selectList(new LambdaQueryWrapper<SpecName>().orderByAsc(SpecName::getSort).orderByAsc(SpecName::getId));
        while (specs.size() < specCount) {
            SpecTemplate template = SPEC_TEMPLATES.get(specs.size() % SPEC_TEMPLATES.size());
            SpecName specName = new SpecName();
            specName.setName(template.name + "-" + (specs.size() + 1));
            specName.setSort(specs.size() + 1);
            specName.setCreatedAt(LocalDateTime.now());
            specName.setUpdatedAt(LocalDateTime.now());
            specNameMapper.insert(specName);
            specs.add(specName);
        }

        List<SpecName> selected = specs.subList(0, specCount);
        for (int i = 0; i < selected.size(); i++) {
            ensureSpecValues(selected.get(i), valuesPerSpec, i);
        }
        return specNameMapper.selectList(new LambdaQueryWrapper<SpecName>().orderByAsc(SpecName::getSort).orderByAsc(SpecName::getId)).subList(0, specCount);
    }

    private void ensureSpecValues(SpecName specName, int valuesPerSpec, int templateIndex) {
        List<SpecValue> values = specValueMapper.selectList(new LambdaQueryWrapper<SpecValue>()
                .eq(SpecValue::getSpecId, specName.getId())
                .orderByAsc(SpecValue::getSort)
                .orderByAsc(SpecValue::getId));
        if (values.size() >= valuesPerSpec) {
            return;
        }

        List<String> sourceValues = SPEC_TEMPLATES.get(templateIndex % SPEC_TEMPLATES.size()).values;
        for (int i = values.size(); i < valuesPerSpec; i++) {
            SpecValue specValue = new SpecValue();
            specValue.setSpecId(specName.getId());
            specValue.setValue(sourceValues.get(i % sourceValues.size()));
            specValue.setSort(i + 1);
            specValue.setCreatedAt(LocalDateTime.now());
            specValue.setUpdatedAt(LocalDateTime.now());
            specValueMapper.insert(specValue);
        }
    }

    private void createSkusForGoods(Goods goods, String batchCode, int goodsIndex, ProductMockDataParam param,
                                    List<SpecName> specs, AtomicInteger skuCount, AtomicInteger snCount) {
        Set<String> combinations = new HashSet<>();
        for (int i = 0; i < param.getSkuPerGoods(); i++) {
            Map<String, String> specJson = buildSpecJson(specs, combinations, i);
            if (specJson == null) {
                break;
            }
            Sku sku = buildSku(goods, batchCode, goodsIndex, i + 1, specJson);
            skuMapper.insert(sku);
            skuCount.incrementAndGet();
            createSnCodes(sku, goods, param.getSnPerSku(), i + 1, snCount);
        }
    }

    private Map<String, String> buildSpecJson(List<SpecName> specs, Set<String> combinations, int offset) {
        Map<String, String> specJson = new LinkedHashMap<>();
        for (int i = 0; i < specs.size(); i++) {
            List<SpecValue> values = specValueMapper.selectList(new LambdaQueryWrapper<SpecValue>()
                    .eq(SpecValue::getSpecId, specs.get(i).getId())
                    .orderByAsc(SpecValue::getSort)
                    .orderByAsc(SpecValue::getId));
            SpecValue specValue = values.get((offset + i) % values.size());
            specJson.put(specs.get(i).getName(), specValue.getValue());
        }

        String key = specJson.toString();
        if (!combinations.add(key)) {
            return null;
        }
        return specJson;
    }

    private Sku buildSku(Goods goods, String batchCode, int goodsIndex, int skuIndex, Map<String, String> specJson) {
        Sku sku = new Sku();
        sku.setSpuId(goods.getId());
        sku.setSkuCode(String.format("MOCK-%s-%03d-%02d", batchCode, goodsIndex, skuIndex));
        sku.setSpecJson(toJson(specJson));
        sku.setPrice(buildPrice(goodsIndex, skuIndex));
        sku.setCostPrice(sku.getPrice().subtract(new BigDecimal("120.00")));
        sku.setUnit("件");
        sku.setImageUrl(goods.getImageUrl());
        sku.setStatus(1);
        sku.setCreatedAt(LocalDateTime.now());
        sku.setUpdatedAt(LocalDateTime.now());
        return sku;
    }

    private Goods buildGoods(String batchCode, int goodsIndex, GoodsCategory category) {
        Goods goods = new Goods();
        goods.setName(String.format("模拟商品 %s-%03d", batchCode, goodsIndex));
        goods.setCategoryId(category.getId());
        goods.setBrand(BRAND_NAMES.get(goodsIndex % BRAND_NAMES.size()));
        goods.setImageUrl("https://dummyimage.com/600x600/edf2ff/1f2937&text=MOCK+" + goodsIndex);
        goods.setImages(String.format("[\"https://dummyimage.com/600x600/edf2ff/1f2937&text=MOCK+%d-1\",\"https://dummyimage.com/600x600/e0f2fe/1f2937&text=MOCK+%d-2\"]", goodsIndex, goodsIndex));
        goods.setShortDesc("模拟商品简短描述-" + goodsIndex);
        goods.setDescription("批量生成的模拟商品数据，用于商品、SKU、规格和SN码测试。");
        goods.setStatus(1);
        goods.setCreatedAt(LocalDateTime.now());
        goods.setUpdatedAt(LocalDateTime.now());
        return goods;
    }

    private void createSnCodes(Sku sku, Goods goods, int snPerSku, int skuIndex, AtomicInteger snCount) {
        for (int i = 0; i < snPerSku; i++) {
            SnCode snCode = new SnCode();
            snCode.setSnCode(String.format("%s-SN-%02d-%02d", sku.getSkuCode(), skuIndex, i + 1));
            snCode.setSkuId(sku.getId());
            snCode.setSpuId(goods.getId());
            snCode.setSpuName(goods.getName());
            snCode.setSkuCode(sku.getSkuCode());
            snCode.setSpecJson(sku.getSpecJson());
            snCode.setPrice(sku.getPrice());
            snCode.setStatus(0);
            snCode.setSource(3);
            snCode.setCreatedAt(LocalDateTime.now());
            snCode.setUpdatedAt(LocalDateTime.now());
            snCodeMapper.insert(snCode);
            snCount.incrementAndGet();
        }
    }

    private String toJson(Map<String, String> specJson) {
        try {
            return objectMapper.writeValueAsString(specJson);
        } catch (JsonProcessingException e) {
            throw new BusinessException(500, "规格JSON序列化失败");
        }
    }

    private BigDecimal buildPrice(int goodsIndex, int skuIndex) {
        int base = 599 + goodsIndex * 19 + skuIndex * 7;
        return new BigDecimal(base).setScale(2);
    }

    private String buildBatchCode() {
        return BATCH_FORMATTER.format(LocalDateTime.now()) + "-" + ThreadLocalRandom.current().nextInt(100, 999);
    }

    private static final class SpecTemplate {
        private final String name;
        private final List<String> values;

        private SpecTemplate(String name, List<String> values) {
            this.name = name;
            this.values = values;
        }
    }
}