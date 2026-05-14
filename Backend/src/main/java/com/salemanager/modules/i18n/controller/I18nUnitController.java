package com.salemanager.modules.i18n.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.i18n.model.TranslationUnit;
import com.salemanager.modules.i18n.param.BatchSaveParam;
import com.salemanager.modules.i18n.param.SaveUnitParam;
import com.salemanager.modules.i18n.service.I18nService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/i18n")
@Validated
public class I18nUnitController {

    private static final Logger log = LoggerFactory.getLogger(I18nUnitController.class);
    private final I18nService i18nService;

    public I18nUnitController(I18nService i18nService) {
        this.i18nService = i18nService;
    }

    /** 查询实体的所有最小元 */
    @GetMapping("/units")
    public Result<Map<String, Object>> getUnits(@RequestParam String entityType,
                                                @RequestParam Long entityId) {
        log.info("getUnits entityType={} entityId={}", entityType, entityId);
        List<TranslationUnit> units = i18nService.getUnits(entityType, entityId);
        Map<String, Object> result = Map.of(
            "entityType", entityType,
            "entityId", entityId,
            "units", units
        );
        return Result.success(result);
    }

    /** 保存单个最小元(单语言) */
    @PutMapping("/units/{unitKey}")
    public Result<Map<String, Object>> saveUnit(@PathVariable String unitKey,
                                                @Valid @RequestBody SaveUnitParam param) {
        log.info("saveUnit unitKey={} locale={}", unitKey, param.getLocale());
        i18nService.saveUnit(unitKey, param.getLocale(), param.getValue(), param.getForce() != null && param.getForce());
        return Result.success(Map.of("success", true));
    }

    /** 批量保存实体翻译 */
    @PutMapping("/units/batch")
    public Result<Map<String, Object>> batchSave(@Valid @RequestBody BatchSaveParam param) {
        log.info("batchSave entityType={} entityId={} units={}",
            param.getEntityType(), param.getEntityId(),
            param.getUnits() != null ? param.getUnits().size() : 0);
        if (param.getUnits() == null || param.getUnits().isEmpty()) {
            return Result.success(Map.of("success", true, "count", 0));
        }
        List<Map<String, Object>> data = param.getUnits().stream()
            .map(u -> {
                Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("unitKey", u.getUnitKey());
                m.put("locales", u.getLocales());
                return m;
            })
            .toList();
        i18nService.batchSave(param.getEntityType(), param.getEntityId(), data);
        return Result.success(Map.of("success", true, "count", data.size()));
    }

    /** 删除实体所有翻译 */
    @DeleteMapping("/units")
    public Result<Map<String, Object>> deleteUnits(@RequestParam String entityType,
                                                   @RequestParam Long entityId) {
        log.info("deleteUnits entityType={} entityId={}", entityType, entityId);
        i18nService.deleteByEntity(entityType, entityId);
        return Result.success(Map.of("success", true));
    }

    /** 翻译状态 */
    @GetMapping("/units/status")
    public Result<Map<String, Object>> getStatus(@RequestParam String entityType,
                                                  @RequestParam Long entityId) {
        return Result.success(i18nService.getStatus(entityType, entityId));
    }
}
