package com.salemanager.modules.i18n.service.impl;

import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.i18n.config.I18nProperties;
import com.salemanager.modules.i18n.model.TranslationUnit;
import com.salemanager.modules.i18n.model.TranslationUnit.LocaleEntry;
import com.salemanager.modules.i18n.repository.TranslationUnitRepository;
import com.salemanager.modules.i18n.service.I18nCacheService;
import com.salemanager.modules.i18n.service.I18nService;
import com.salemanager.modules.i18n.service.I18nValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class I18nServiceImpl implements I18nService {

    private static final Logger log = LoggerFactory.getLogger(I18nServiceImpl.class);

    private final TranslationUnitRepository repo;
    private final MongoTemplate mongoTemplate;
    private final I18nProperties properties;
    private final I18nCacheService cache;
    private final I18nValidationService validator;

    public I18nServiceImpl(TranslationUnitRepository repo, MongoTemplate mongoTemplate,
                           I18nProperties properties,
                           I18nCacheService cache, I18nValidationService validator) {
        this.repo = repo;
        this.mongoTemplate = mongoTemplate;
        this.properties = properties;
        this.cache = cache;
        this.validator = validator;
    }

    @Override
    public List<TranslationUnit> getUnits(String entityType, Long entityId) {
        List<TranslationUnit> cached = cache.getEntityUnits(entityType, entityId);
        if (cached != null) return cached;

        List<TranslationUnit> units = repo.findByEntityTypeAndEntityIdOrderBySortOrderAsc(entityType, entityId);
        cache.putEntityUnits(entityType, entityId, units);
        return units;
    }

    @Override
    @Transactional
    public void saveUnit(String unitKey, String locale, Object value, boolean force) {
        TranslationUnit unit = repo.findByUnitKey(unitKey);
        if (unit == null) {
            throw new BusinessException(404, "翻译单元不存在: " + unitKey);
        }

        validator.checkOverwriteAllowed(unit, locale, force);
        validator.validateValue(unit, locale, value);

        unit.getLocales().computeIfAbsent(locale, k -> new LocaleEntry());
        LocaleEntry entry = unit.getLocales().get(locale);
        entry.setValue(value);
        entry.setStatus(force || entry.getStatus() == null ? "translated" : entry.getStatus());
        entry.setUpdatedAt(LocalDateTime.now());
        unit.setUpdatedAt(LocalDateTime.now());

        repo.save(unit);
        cache.evictUnit(unitKey);
        cache.evictEntity(unit.getEntityType(), unit.getEntityId());
        log.info("saveUnit {} locale={}", unitKey, locale);
    }

    @Override
    @Transactional
    public void batchSave(String entityType, Long entityId, List<Map<String, Object>> unitData) {
        for (Map<String, Object> data : unitData) {
            String unitKey = (String) data.get("unitKey");
            Map<String, Map<String, Object>> locales = (Map<String, Map<String, Object>>) data.get("locales");
            if (locales != null) {
                for (var le : locales.entrySet()) {
                    String locale = le.getKey();
                    Object value = le.getValue().get("value");
                    saveUnit(unitKey, locale, value, false);
                }
            }
        }
        cache.evictEntity(entityType, entityId);
    }

    @Override
    @Transactional
    public void deleteByEntity(String entityType, Long entityId) {
        repo.deleteByEntityTypeAndEntityId(entityType, entityId);
        cache.evictEntity(entityType, entityId);
        log.info("deleteByEntity {}:{}", entityType, entityId);
    }

    @Override
    public Map<String, Object> getStatus(String entityType, Long entityId) {
        List<TranslationUnit> units = getUnits(entityType, entityId);
        int total = units.size();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("entityType", entityType);
        result.put("entityId", entityId);
        result.put("totalUnits", total);

        Map<String, Object> localesStatus = new LinkedHashMap<>();
        for (String loc : properties.getSupportedLocales()) {
            int translated = 0;
            int outdated = 0;
            for (TranslationUnit unit : units) {
                LocaleEntry e = unit.getLocales().get(loc);
                if (e != null && e.getValue() != null && !e.getValue().toString().isEmpty()
                    && !"draft".equals(e.getStatus())) {
                    translated++;
                }
                // 检查是否过时
                String baseHash = unit.getBaseContentHash();
                if (baseHash != null && e != null && !"approved".equals(e.getStatus())) {
                    outdated++;
                }
            }
            Map<String, Object> status = new LinkedHashMap<>();
            status.put("completeness", total == 0 ? 0 : translated * 100 / total);
            status.put("outdated", outdated);
            status.put("total", total);
            localesStatus.put(loc, status);
        }
        result.put("localesStatus", localesStatus);
        return result;
    }

    @Override
    public Map<String, Object> assembleEntity(String entityType, Long entityId, String locale) {
        List<TranslationUnit> units = getUnits(entityType, entityId);
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> attrs = new LinkedHashMap<>();

        for (TranslationUnit unit : units) {
            Object value = resolveLocaleValue(unit, locale);
            String path = unit.getFieldPath();

            if (path.startsWith("attrs_")) {
                attrs.put(path.substring(6), value);
            } else {
                result.put(path, value);
            }
        }
        if (!attrs.isEmpty()) {
            result.put("attrs", attrs);
        }
        return result;
    }

    @Override
    public Object resolveLocaleValue(TranslationUnit unit, String requestedLocale) {
        // 1. 请求语言
        LocaleEntry entry = unit.getLocales().get(requestedLocale);
        if (entry != null && entry.getValue() != null && !isEmptyString(entry.getValue())) {
            return entry.getValue();
        }

        // 2. Fallback 链
        List<String> chain = properties.getFallbackChain().get(requestedLocale);
        if (chain != null) {
            for (String fallback : chain) {
                LocaleEntry fb = unit.getLocales().get(fallback);
                if (fb != null && fb.getValue() != null && !isEmptyString(fb.getValue())) {
                    return fb.getValue();
                }
            }
        }

        // 3. 默认语言
        LocaleEntry def = unit.getLocales().get(properties.getDefaultLocale());
        if (def != null && def.getValue() != null) {
            return def.getValue();
        }

        return "";
    }

    @Override
    public Map<String, Object> getUnitsPaged(String entityType, String keyword, int page, int pageSize) {
        Criteria criteria = new Criteria();
        List<Criteria> andCriteria = new ArrayList<>();

        if (entityType != null && !entityType.isEmpty()) {
            andCriteria.add(Criteria.where("entityType").is(entityType));
        }
        if (keyword != null && !keyword.isEmpty()) {
            Pattern pattern = Pattern.compile(".*" + Pattern.quote(keyword) + ".*", Pattern.CASE_INSENSITIVE);
            andCriteria.add(new Criteria().orOperator(
                Criteria.where("unitKey").regex(pattern),
                Criteria.where("name").regex(pattern),
                Criteria.where("fieldPath").regex(pattern)
            ));
        }

        if (!andCriteria.isEmpty()) {
            criteria.andOperator(andCriteria.toArray(new Criteria[0]));
        }

        Query query = Query.query(criteria);
        long total = mongoTemplate.count(query, TranslationUnit.class);

        query.with(PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "updatedAt")));
        List<TranslationUnit> items = mongoTemplate.find(query, TranslationUnit.class);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("items", items);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public List<String> getSupportedLocales() {
        return properties.getSupportedLocales();
    }

    private boolean isEmptyString(Object value) {
        return value instanceof String s && s.isEmpty();
    }
}
