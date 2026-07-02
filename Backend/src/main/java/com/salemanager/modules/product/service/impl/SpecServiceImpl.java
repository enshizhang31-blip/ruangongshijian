package com.salemanager.modules.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.i18n.service.I18nSyncService;
import com.salemanager.modules.product.mapper.SkuMapper;
import com.salemanager.modules.product.mapper.SpecNameMapper;
import com.salemanager.modules.product.mapper.SpecValueMapper;
import com.salemanager.modules.product.model.Sku;
import com.salemanager.modules.product.model.SpecName;
import com.salemanager.modules.product.model.SpecValue;
import com.salemanager.modules.product.param.SpecParam;
import com.salemanager.modules.product.param.SpecValueParam;
import com.salemanager.modules.product.service.SpecService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 规格服务实现
 */
@Service
public class SpecServiceImpl implements SpecService {

    private static final Logger log = LoggerFactory.getLogger(SpecServiceImpl.class);

    @Autowired
    private SpecNameMapper specNameMapper;

    @Autowired
    private SpecValueMapper specValueMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private I18nSyncService i18nSyncService;

    @Override
    public List<SpecName> getSpecList() {
        log.info("getSpecList");
        return specNameMapper.selectList(new LambdaQueryWrapper<SpecName>()
                .orderByAsc(SpecName::getSort)
                .orderByAsc(SpecName::getId));
    }

    @Override
    @Transactional
    public void createSpec(SpecParam param) {
        log.info("createSpec name={}", param.getName());
        validateNameUnique(param.getName(), null);

        SpecName spec = new SpecName();
        spec.setName(param.getName());
        spec.setCategoryId(param.getCategoryId());
        spec.setSort(param.getSort() != null ? param.getSort() : 0);
        spec.setCreatedAt(LocalDateTime.now());
        spec.setUpdatedAt(LocalDateTime.now());

        specNameMapper.insert(spec);
        i18nSyncService.syncSpecCreated(spec.getId(), spec.getName());
        log.info("规格创建成功 id={}, name={}", spec.getId(), spec.getName());
    }

    @Override
    @Transactional
    public void updateSpec(Long id, SpecParam param) {
        log.info("updateSpec id={}", id);
        SpecName spec = getSpecById(id);
        validateNameUnique(param.getName(), id);

        spec.setName(param.getName());
        spec.setCategoryId(param.getCategoryId());
        spec.setSort(param.getSort() != null ? param.getSort() : spec.getSort());
        spec.setUpdatedAt(LocalDateTime.now());

        specNameMapper.updateById(spec);
        log.info("规格更新成功 id={}", id);
    }

    @Override
    @Transactional
    public void deleteSpec(Long id) {
        log.info("deleteSpec id={}", id);
        getSpecById(id);

        // 检查规格值是否被SKU引用
        List<SpecValue> values = specValueMapper.selectList(
                new LambdaQueryWrapper<SpecValue>().eq(SpecValue::getSpecId, id));
        for (SpecValue sv : values) {
            checkSkuReference(sv);
        }

        specValueMapper.delete(new LambdaQueryWrapper<SpecValue>().eq(SpecValue::getSpecId, id));
        specNameMapper.deleteById(id);
        log.info("规格删除成功 id={}", id);
    }

    @Override
    public List<SpecValue> getSpecValues(Long specId) {
        log.info("getSpecValues specId={}", specId);
        getSpecById(specId);
        return specValueMapper.selectList(new LambdaQueryWrapper<SpecValue>()
                .eq(SpecValue::getSpecId, specId)
                .orderByAsc(SpecValue::getSort)
                .orderByAsc(SpecValue::getId));
    }

    @Override
    @Transactional
    public void createSpecValue(Long specId, SpecValueParam param) {
        log.info("createSpecValue specId={}, value={}", specId, param.getValue());
        getSpecById(specId);
        validateValueUnique(specId, param.getValue(), null);

        SpecValue specValue = new SpecValue();
        specValue.setSpecId(specId);
        specValue.setValue(param.getValue());
        specValue.setSort(param.getSort() != null ? param.getSort() : 0);
        specValue.setCreatedAt(LocalDateTime.now());
        specValue.setUpdatedAt(LocalDateTime.now());

        specValueMapper.insert(specValue);
        i18nSyncService.syncSpecValueCreated(specId, specValue.getId(), specValue.getValue());
        log.info("规格值创建成功 id={}, specId={}", specValue.getId(), specId);
    }

    @Override
    @Transactional
    public List<SpecValue> batchCreateSpecValues(Long specId, List<String> values) {
        log.info("batchCreateSpecValues specId={}, values={}", specId, values);
        getSpecById(specId);

        if (values == null || values.isEmpty()) {
            throw new BusinessException(400, "规格值列表不能为空");
        }

        // 1. 过滤空值和重复值, 保持顺序
        java.util.LinkedHashSet<String> uniqueValues = new java.util.LinkedHashSet<>();
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                uniqueValues.add(v.trim());
            }
        }
        if (uniqueValues.isEmpty()) {
            throw new BusinessException(400, "规格值列表不能为空");
        }

        // 2. 查询已存在的值, 用于去重提示
        List<SpecValue> existing = specValueMapper.selectList(
                new LambdaQueryWrapper<SpecValue>().eq(SpecValue::getSpecId, specId));
        java.util.Set<String> existingValues = new java.util.HashSet<>();
        for (SpecValue ev : existing) {
            existingValues.add(ev.getValue());
        }

        // 3. 获取当前最大 sort, 后续按顺序累加
        int baseSort = existing.stream()
                .mapToInt(s -> s.getSort() != null ? s.getSort() : 0)
                .max()
                .orElse(0);

        // 4. 逐个创建 (跳过已存在)
        List<SpecValue> created = new java.util.ArrayList<>();
        List<String> skipped = new java.util.ArrayList<>();
        int order = 0;
        for (String value : uniqueValues) {
            if (existingValues.contains(value)) {
                skipped.add(value);
                continue;
            }
            SpecValue specValue = new SpecValue();
            specValue.setSpecId(specId);
            specValue.setValue(value);
            specValue.setSort(baseSort + order + 1);
            specValue.setCreatedAt(LocalDateTime.now());
            specValue.setUpdatedAt(LocalDateTime.now());
            specValueMapper.insert(specValue);
            i18nSyncService.syncSpecValueCreated(specId, specValue.getId(), specValue.getValue());
            created.add(specValue);
            order++;
        }
        log.info("批量创建规格值完成 specId={}, 新增={}, 跳过={}",
                specId, created.size(), skipped.size());
        return created;
    }

    @Override
    @Transactional
    public void updateSpecValue(Long id, SpecValueParam param) {
        log.info("updateSpecValue id={}", id);
        SpecValue specValue = getSpecValueById(id);
        validateValueUnique(specValue.getSpecId(), param.getValue(), id);

        specValue.setValue(param.getValue());
        specValue.setSort(param.getSort() != null ? param.getSort() : specValue.getSort());
        specValue.setUpdatedAt(LocalDateTime.now());

        specValueMapper.updateById(specValue);
        log.info("规格值更新成功 id={}", id);
    }

    @Override
    @Transactional
    public void deleteSpecValue(Long id) {
        log.info("deleteSpecValue id={}", id);
        SpecValue specValue = getSpecValueById(id);

        // 安全检查：规格值是否被SKU引用
        checkSkuReference(specValue);

        specValueMapper.deleteById(id);
        log.info("规格值删除成功 id={}", id);
    }

    /** 检查规格值是否被SKU的specJson引用 */
    private void checkSkuReference(SpecValue specValue) {
        Long count = skuMapper.selectCount(
                new LambdaQueryWrapper<Sku>()
                        .like(Sku::getSpecJson, specValue.getValue()));
        if (count != null && count > 0) {
            throw new BusinessException(
                    String.format("规格值\"%s\"已被 %d 个SKU使用，无法删除", specValue.getValue(), count));
        }
    }

    private SpecName getSpecById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "规格ID无效");
        }

        SpecName spec = specNameMapper.selectById(id);
        if (spec == null) {
            throw new BusinessException(404, "规格不存在");
        }
        return spec;
    }

    private SpecValue getSpecValueById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(400, "规格值ID无效");
        }

        SpecValue specValue = specValueMapper.selectById(id);
        if (specValue == null) {
            throw new BusinessException(404, "规格值不存在");
        }
        return specValue;
    }

    private void validateNameUnique(String name, Long excludeId) {
        if (!StringUtils.hasText(name)) {
            throw new BusinessException(400, "规格名称不能为空");
        }

        LambdaQueryWrapper<SpecName> wrapper = new LambdaQueryWrapper<SpecName>()
                .eq(SpecName::getName, name);
        if (excludeId != null) {
            wrapper.ne(SpecName::getId, excludeId);
        }

        Long count = specNameMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException(400, "规格名称已存在");
        }
    }

    private void validateValueUnique(Long specId, String value, Long excludeId) {
        if (!StringUtils.hasText(value)) {
            throw new BusinessException(400, "规格值不能为空");
        }

        LambdaQueryWrapper<SpecValue> wrapper = new LambdaQueryWrapper<SpecValue>()
                .eq(SpecValue::getSpecId, specId)
                .eq(SpecValue::getValue, value);
        if (excludeId != null) {
            wrapper.ne(SpecValue::getId, excludeId);
        }

        Long count = specValueMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException(400, "规格值已存在");
        }
    }

    @Override
    public List<Map<String, Object>> resolveSpecItems(List<Map<String, Long>> items) {
        log.info("resolveSpecItems count={}", items.size());
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (var item : items) {
            Long specId = item.get("specId");
            Long valueId = item.get("valueId");
            java.util.Map<String, Object> resolved = new java.util.LinkedHashMap<>();
            resolved.put("specId", specId);
            resolved.put("valueId", valueId);
            if (specId != null) {
                SpecName spec = specNameMapper.selectById(specId);
                resolved.put("specName", spec != null ? spec.getName() : null);
            }
            if (valueId != null) {
                SpecValue val = specValueMapper.selectById(valueId);
                resolved.put("valueName", val != null ? val.getValue() : null);
            }
            result.add(resolved);
        }
        return result;
    }
}