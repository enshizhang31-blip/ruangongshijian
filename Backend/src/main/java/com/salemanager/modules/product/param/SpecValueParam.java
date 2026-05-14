package com.salemanager.modules.product.param;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 规格值请求参数
 */
@Data
public class SpecValueParam {

    @NotBlank(message = "规格值不能为空")
    @Size(max = 128, message = "规格值不能超过128个字符")
    private String value;

    @Min(value = 0, message = "排序值无效")
    @Max(value = 9999, message = "排序值不能超过9999")
    private Integer sort;
}