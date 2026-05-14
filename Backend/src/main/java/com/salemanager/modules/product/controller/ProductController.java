package com.salemanager.modules.product.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.product.model.Goods;
import com.salemanager.modules.product.model.Sku;
import com.salemanager.modules.product.param.ProductParam;
import com.salemanager.modules.product.service.ProductService;
import com.salemanager.modules.product.service.SkuService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/api/admin/product")
@Validated
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private SkuService skuService;

    /**
     * 获取商品列表
     */
    @GetMapping
    public Result<Map<String, Object>> getProductList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        log.info("getProductList keyword={}, status={}, page={}, pageSize={}", keyword, status, page, pageSize);

        List<Goods> list = productService.getProductList(keyword, status, page, pageSize);
        Long total = productService.getProductCount(keyword, status);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("pagination", Map.of(
                "page", page,
                "pageSize", pageSize,
                "total", total
        ));

        return Result.success(result);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public Result<Goods> getProductById(@PathVariable Long id) {
        log.info("getProductById id={}", id);
        Goods goods = productService.getProductById(id);
        return Result.success(goods);
    }

    /**
     * 获取商品SKU列表
     */
    @GetMapping("/{spuId}/sku")
    public Result<List<Sku>> getSkuList(@PathVariable Long spuId) {
        log.info("getSkuList spuId={}", spuId);
        List<Sku> list = skuService.getSkuListBySpuId(spuId);
        return Result.success(list);
    }

    /**
     * 新增商品
     */
    @PostMapping
    public Result<Void> createProduct(@Valid @RequestBody ProductParam param) {
        log.info("createProduct name={}", param.getName());
        productService.createProduct(param);
        return Result.success();
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public Result<Void> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductParam param) {
        log.info("updateProduct id={}", id);
        productService.updateProduct(id, param);
        return Result.success();
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        log.info("deleteProduct id={}", id);
        productService.deleteProduct(id);
        return Result.success();
    }
}
