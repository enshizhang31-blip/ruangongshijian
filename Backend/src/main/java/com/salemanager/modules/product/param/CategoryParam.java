package com.salemanager.modules.product.param;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 商品分类请求参数
 */
@Data
public class CategoryParam {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称不能超过50个字符")
    private String name;

    @Positive(message = "父分类ID必须是正数")
    private Long parentId;

    @Size(max = 500, message = "图标URL不能超过500个字符")
    private String icon;

    @Min(value = 0, message = "排序值无效")
    @Max(value = 9999, message = "排序值不能超过9999")
    private Integer sort;

    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    private Integer status;
}
