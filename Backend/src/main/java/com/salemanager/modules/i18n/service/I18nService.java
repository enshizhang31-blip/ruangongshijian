package com.salemanager.modules.i18n.service;

import com.salemanager.modules.i18n.model.TranslationUnit;

import java.util.List;
import java.util.Map;

public interface I18nService {

    /** 查询实体所有最小元 */
    List<TranslationUnit> getUnits(String entityType, Long entityId);

    /** 保存单个最小元的单语言翻译 */
    void saveUnit(String unitKey, String locale, Object value, boolean force);

    /** 批量保存翻译 */
    void batchSave(String entityType, Long entityId, List<Map<String, Object>> unitData);

    /** 删除实体所有翻译 */
    void deleteByEntity(String entityType, Long entityId);

    /** 获取翻译状态 */
    Map<String, Object> getStatus(String entityType, Long entityId);

    /** 组装实体翻译（含 fallback） */
    Map<String, Object> assembleEntity(String entityType, Long entityId, String locale);

    /** 解析单个最小元的 locale 值（含 fallback） */
    Object resolveLocaleValue(TranslationUnit unit, String requestedLocale);

    /** 分页查询翻译单元 */
    Map<String, Object> getUnitsPaged(String entityType, String keyword, int page, int pageSize);

    /** 获取支持的语言列表 */
    List<String> getSupportedLocales();
}
