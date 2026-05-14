package com.salemanager.modules.product.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.product.param.ProductMockDataParam;
import com.salemanager.modules.product.service.ProductMockDataService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 商品模拟数据控制器
 */
@RestController
@RequestMapping("/api/admin/product")
@Validated
public class ProductMockDataController {

    private static final Logger log = LoggerFactory.getLogger(ProductMockDataController.class);

    @Autowired
    private ProductMockDataService productMockDataService;

    /**
     * 批量生成商品模拟数据
     */
    @PostMapping("/mock-data")
    public Result<Map<String, Object>> generateMockData(@Valid @RequestBody ProductMockDataParam param) {
        log.info("generateMockData goodsCount={}, skuPerGoods={}, snPerSku={}",
                param.getGoodsCount(), param.getSkuPerGoods(), param.getSnPerSku());
        Map<String, Object> result = productMockDataService.generateMockData(param);
        return Result.success(result);
    }
}