package com.salemanager.modules.product.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.product.model.Goods;
import com.salemanager.modules.product.param.ProductParam;
import com.salemanager.modules.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/api/admin/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 获取商品列表
     */
    @GetMapping
    public Result<Map<String, Object>> getProductList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

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
        Goods goods = productService.getProductById(id);
        return Result.success(goods);
    }

    /**
     * 新增商品
     */
    @PostMapping
    public Result<Void> createProduct(@RequestBody ProductParam param) {
        productService.createProduct(param);
        return Result.success();
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public Result<Void> updateProduct(@PathVariable Long id, @RequestBody ProductParam param) {
        productService.updateProduct(id, param);
        return Result.success();
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success();
    }
}
