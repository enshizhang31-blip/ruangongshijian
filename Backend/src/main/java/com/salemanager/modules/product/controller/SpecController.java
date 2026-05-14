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
}