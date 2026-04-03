package com.salemanager.modules.product.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.product.model.GoodsCategory;
import com.salemanager.modules.product.param.CategoryParam;
import com.salemanager.modules.product.service.CategoryService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 */
@RestController
@RequestMapping("/api/admin/product/categories")
@Validated
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取分类列表
     */
    @GetMapping
    public Result<List<GoodsCategory>> getCategoryList() {
        log.info("getCategoryList");
        List<GoodsCategory> list = categoryService.getCategoryList();
        return Result.success(list);
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    public Result<GoodsCategory> getCategoryById(@PathVariable Long id) {
        log.info("getCategoryById id={}", id);
        GoodsCategory category = categoryService.getCategoryById(id);
        return Result.success(category);
    }

    /**
     * 新增分类
     */
    @PostMapping
    public Result<Void> createCategory(@Valid @RequestBody CategoryParam param) {
        log.info("createCategory name={}", param.getName());
        categoryService.createCategory(param);
        return Result.success();
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryParam param) {
        log.info("updateCategory id={}", id);
        categoryService.updateCategory(id, param);
        return Result.success();
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        log.info("deleteCategory id={}", id);
        categoryService.deleteCategory(id);
        return Result.success();
    }
}
