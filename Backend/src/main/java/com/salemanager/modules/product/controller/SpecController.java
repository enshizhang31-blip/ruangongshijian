package com.salemanager.modules.product.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.product.model.SpecName;
import com.salemanager.modules.product.model.SpecValue;
import com.salemanager.modules.product.param.SpecParam;
import com.salemanager.modules.product.param.SpecValueParam;
import com.salemanager.modules.product.service.SpecService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 规格控制器
 */
@RestController
@RequestMapping("/api/admin/spec")
@Validated
public class SpecController {

    private static final Logger log = LoggerFactory.getLogger(SpecController.class);

    @Autowired
    private SpecService specService;

    /**
     * 获取规格列表
     */
    @GetMapping
    public Result<List<SpecName>> getSpecList() {
        log.info("getSpecList");
        List<SpecName> list = specService.getSpecList();
        return Result.success(list);
    }

    /**
     * 新增规格
     */
    @PostMapping
    public Result<Void> createSpec(@Valid @RequestBody SpecParam param) {
        log.info("createSpec name={}", param.getName());
        specService.createSpec(param);
        return Result.success();
    }

    /**
     * 更新规格
     */
    @PutMapping("/{id}")
    public Result<Void> updateSpec(@PathVariable Long id, @Valid @RequestBody SpecParam param) {
        log.info("updateSpec id={}", id);
        specService.updateSpec(id, param);
        return Result.success();
    }

    /**
     * 删除规格
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteSpec(@PathVariable Long id) {
        log.info("deleteSpec id={}", id);
        specService.deleteSpec(id);
        return Result.success();
    }

    /**
     * 获取规格值
     */
    @GetMapping("/{specId}/value")
    public Result<List<SpecValue>> getSpecValues(@PathVariable Long specId) {
        log.info("getSpecValues specId={}", specId);
        List<SpecValue> list = specService.getSpecValues(specId);
        return Result.success(list);
    }

    /**
     * 新增规格值
     */
    @PostMapping("/{specId}/value")
    public Result<Void> createSpecValue(@PathVariable Long specId, @Valid @RequestBody SpecValueParam param) {
        log.info("createSpecValue specId={}, value={}", specId, param.getValue());
        specService.createSpecValue(specId, param);
        return Result.success();
    }

    /**
     * 批量添加规格值 (供"添加值"窗口批量录入使用)
     * POST /api/admin/spec/{specId}/values  body: { "values": ["黑色", "白色", "红色"] }
     */
    @PostMapping("/{specId}/values")
    public Result<List<SpecValue>> batchCreateSpecValues(
            @PathVariable Long specId,
            @RequestBody Map<String, Object> body) {
        Object valuesObj = body.get("values");
        List<String> values = new java.util.ArrayList<>();
        if (valuesObj instanceof List) {
            for (Object o : (List<?>) valuesObj) {
                if (o != null) {
                    String v = String.valueOf(o).trim();
                    if (!v.isEmpty()) values.add(v);
                }
            }
        } else if (valuesObj instanceof String) {
            // 兼容逗号/换行分隔的字符串
            String s = (String) valuesObj;
            for (String line : s.split("[,\n]")) {
                String t = line.trim();
                if (!t.isEmpty()) values.add(t);
            }
        }
        if (values.isEmpty()) {
            return Result.fail(400, "规格值列表不能为空");
        }
        log.info("batchCreateSpecValues specId={}, count={}", specId, values.size());
        List<SpecValue> list = specService.batchCreateSpecValues(specId, values);
        return Result.success(list);
    }

    /**
     * 更新规格值
     */
    @PutMapping("/value/{id}")
    public Result<Void> updateSpecValue(@PathVariable Long id, @Valid @RequestBody SpecValueParam param) {
        log.info("updateSpecValue id={}", id);
        specService.updateSpecValue(id, param);
        return Result.success();
    }

    /**
     * 删除规格值
     */
    @DeleteMapping("/value/{id}")
    public Result<Void> deleteSpecValue(@PathVariable Long id) {
        log.info("deleteSpecValue id={}", id);
        specService.deleteSpecValue(id);
        return Result.success();
    }

    /**
     * 根据规格ID和值ID批量解析为可读名称
     * 请求: POST /api/admin/spec/resolve  body: [{"specId":1,"valueId":1}, ...]
     */
    @PostMapping("/resolve")
    public Result<Map<String, Object>> resolveSpec(@RequestBody List<Map<String, Long>> items) {
        log.info("resolveSpec count={}", items.size());
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("items", specService.resolveSpecItems(items));
        return Result.success(result);
    }
}