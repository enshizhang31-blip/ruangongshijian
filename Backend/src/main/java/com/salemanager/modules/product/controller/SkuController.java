package com.salemanager.modules.product.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.product.model.Sku;
import com.salemanager.modules.product.param.BatchGenerateSkuParam;
import com.salemanager.modules.product.param.SkuParam;
import com.salemanager.modules.product.service.SkuService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
     * 删除SKU（校验是否有在库SN码，级联删除SN码和日志）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteSku(@PathVariable Long id) {
        log.info("deleteSku id={}", id);
        skuService.deleteSku(id);
        return Result.success();
    }

    /**
     * 批量生成SKU（笛卡尔积）
     * 输入：SPU ID + 规格ID列表 → 系统自动计算所有规格组合并生成SKU
     */
    @PostMapping("/batch-generate")
    public Result<List<Sku>> batchGenerateSkus(@Valid @RequestBody BatchGenerateSkuParam param) {
        log.info("batchGenerateSkus spuId={}, specIds={}", param.getSpuId(), param.getSpecIds());
        List<Sku> skus = skuService.batchGenerateSkus(param);
        return Result.success(skus);
    }
}