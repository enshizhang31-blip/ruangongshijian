package com.salemanager.modules.product.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.product.param.SkuParam;
import com.salemanager.modules.product.service.SkuService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * SKU控制器
 */
@RestController
@RequestMapping("/api/admin/sku")
@Validated
public class SkuController {

    private static final Logger log = LoggerFactory.getLogger(SkuController.class);

    @Autowired
    private SkuService skuService;

    /**
     * 新增SKU
     */
    @PostMapping
    public Result<Void> createSku(@Valid @RequestBody SkuParam param) {
        log.info("createSku spuId={}, skuCode={}", param.getSpuId(), param.getSkuCode());
        skuService.createSku(param);
        return Result.success();
    }

    /**
     * 更新SKU
     */
    @PutMapping
    public Result<Void> updateSku(@Valid @RequestBody SkuParam param) {
        log.info("updateSku id={}", param.getId());
        skuService.updateSku(param);
        return Result.success();
    }

    /**
     * 删除SKU
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteSku(@PathVariable Long id) {
        log.info("deleteSku id={}", id);
        skuService.deleteSku(id);
        return Result.success();
    }
}