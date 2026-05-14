package com.salemanager.modules.product.param;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 规格名称请求参数
 */
@Data
public class SpecParam {

    @NotBlank(message = "规格名称不能为空")
    @Size(max = 64, message = "规格名称不能超过64个字符")
    private String name;

    @Positive(message = "分类ID必须是正数")
    private Long categoryId;

    @Min(value = 0, message = "排序值无效")
    @Max(value = 9999, message = "排序值不能超过9999")
    private Integer sort;
}