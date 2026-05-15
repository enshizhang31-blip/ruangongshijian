package com.salemanager.modules.i18n.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.i18n.model.TranslationUnit;
import com.salemanager.modules.i18n.param.BatchSaveParam;
import com.salemanager.modules.i18n.param.SaveUnitParam;
import com.salemanager.modules.i18n.service.I18nEntityService;
import com.salemanager.modules.i18n.service.I18nService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/i18n")
@Validated
public class I18nUnitController {

    private static final Logger log = LoggerFactory.getLogger(I18nUnitController.class);
    private final I18nService i18nService;
    private final I18nEntityService i18nEntityService;

    public I18nUnitController(I18nService i18nService, I18nEntityService i18nEntityService) {
        this.i18nService = i18nService;
        this.i18nEntityService = i18nEntityService;
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

    /** 分页列表 */
    @GetMapping("/units/list")
    public Result<Map<String, Object>> getUnitsPaged(@RequestParam(defaultValue = "") String entityType,
                                                     @RequestParam(defaultValue = "") String keyword,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int pageSize) {
        if (pageSize > 200) pageSize = 200;
        return Result.success(i18nService.getUnitsPaged(
            entityType.isEmpty() ? null : entityType,
            keyword.isEmpty() ? null : keyword,
            page, pageSize));
    }

    /** 获取支持的语言列表 */
    @GetMapping("/locales")
    public Result<List<String>> getLocales() {
        return Result.success(i18nService.getSupportedLocales());
    }

    /** 统一搜索实体（从MySQL） */
    @GetMapping("/entities")
    public Result<Map<String, Object>> searchEntities(@RequestParam(defaultValue = "") String entityType,
                                                      @RequestParam(defaultValue = "") String keyword,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int pageSize) {
        return Result.success(i18nEntityService.searchEntities(
            entityType.isEmpty() ? null : entityType,
            keyword.isEmpty() ? null : keyword,
            page, pageSize));
    }

    /** 获取实体字段详情（MySQL数据 + MongoDB翻译合并） */
    @GetMapping("/entities/{entityType}/{entityId}")
    public Result<Map<String, Object>> getEntityFields(@PathVariable String entityType,
                                                       @PathVariable Long entityId) {
        return Result.success(i18nEntityService.getEntityFields(entityType, entityId));
    }

    /** 导出翻译 (JSON) */
    @GetMapping("/units/export")
    public void exportJson(@RequestParam String entityType,
                           @RequestParam(defaultValue = "zh-CN,en-US,ja-JP") String locales,
                           HttpServletResponse response) throws IOException {
        List<TranslationUnit> units = i18nService.getUnits(entityType, null);
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=i18n_" + entityType + ".json");
        PrintWriter w = response.getWriter();
        w.println("{\"entityType\":\"" + entityType + "\",\"exportedAt\":\"" + java.time.LocalDateTime.now() + "\",\"units\":[");
        for (int i = 0; i < units.size(); i++) {
            if (i > 0) w.print(",");
            w.print(units.get(i).toString());
        }
        w.println("]}");
    }

    /** 导出翻译 (CSV) */
    @GetMapping(value = "/units/export/csv", produces = "text/csv;charset=UTF-8")
    public void exportCsv(@RequestParam String entityType, HttpServletResponse response) throws IOException {
        List<TranslationUnit> units = i18nService.getUnits(entityType, null);
        response.setHeader("Content-Disposition", "attachment; filename=i18n_" + entityType + ".csv");
        PrintWriter w = response.getWriter();
        w.println("unit_key,entity_type,entity_id,name,field_type,zh-CN,en-US,ja-JP");
        for (TranslationUnit u : units) {
            w.printf("\"%s\",\"%s\",%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                u.getUnitKey(), u.getEntityType(), u.getEntityId(), u.getName(), u.getFieldType(),
                safeGet(u, "zh-CN"), safeGet(u, "en-US"), safeGet(u, "ja-JP"));
        }
    }

    private String safeGet(TranslationUnit u, String loc) {
        var e = u.getLocales().get(loc);
        return e != null && e.getValue() != null ? e.getValue().toString().replace("\"", "\"\"") : "";
    }
}
