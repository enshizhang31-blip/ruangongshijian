package com.salemanager.modules.product.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.product.model.GoodsCategory;
import com.salemanager.modules.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 */
@RestController
@RequestMapping("/api/admin/product/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取分类列表
     */
    @GetMapping
    public Result<List<GoodsCategory>> getCategoryList() {
        List<GoodsCategory> list = categoryService.getCategoryList();
        return Result.success(list);
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    public Result<GoodsCategory> getCategoryById(@PathVariable Long id) {
        GoodsCategory category = categoryService.getCategoryById(id);
        return Result.success(category);
    }

    /**
     * 新增分类
     */
    @PostMapping
    public Result<Void> createCategory(@RequestBody GoodsCategory category) {
        categoryService.createCategory(category);
        return Result.success();
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody GoodsCategory category) {
        categoryService.updateCategory(id, category);
        return Result.success();
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}
