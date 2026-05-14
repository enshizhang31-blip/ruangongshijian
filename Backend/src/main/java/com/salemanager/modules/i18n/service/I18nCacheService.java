package com.salemanager.modules.i18n.service;

import com.salemanager.modules.i18n.config.I18nProperties;
import com.salemanager.modules.i18n.model.TranslationUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class I18nCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final I18nProperties properties;

    public I18nCacheService(RedisTemplate<String, Object> redisTemplate, I18nProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    // ---- 单个最小元缓存 ----

    public TranslationUnit getUnit(String unitKey) {
        return (TranslationUnit) redisTemplate.opsForValue().get("i18n:unit:" + unitKey);
    }

    public void putUnit(String unitKey, TranslationUnit unit) {
        redisTemplate.opsForValue().set("i18n:unit:" + unitKey, unit,
            properties.getCache().getUnitTtl(), TimeUnit.SECONDS);
    }

    public void evictUnit(String unitKey) {
        redisTemplate.delete("i18n:unit:" + unitKey);
    }

    // ---- 实体翻译缓存 ----

    public List<TranslationUnit> getEntityUnits(String entityType, Long entityId) {
        String key = "i18n:entity:" + entityType + ":" + entityId + ":all";
        return (List<TranslationUnit>) redisTemplate.opsForValue().get(key);
    }

    public void putEntityUnits(String entityType, Long entityId, List<TranslationUnit> units) {
        String key = "i18n:entity:" + entityType + ":" + entityId + ":all";
        redisTemplate.opsForValue().set(key, units, properties.getCache().getEntityTtl(), TimeUnit.SECONDS);
    }

    public void evictEntity(String entityType, Long entityId) {
        String pattern = "i18n:entity:" + entityType + ":" + entityId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
