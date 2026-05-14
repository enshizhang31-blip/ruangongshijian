package com.salemanager.modules.product.param;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * SKU请求参数
 */
@Data
public class SkuParam {

    private Long id;

    @NotNull(message = "商品ID不能为空")
    @Positive(message = "商品ID必须是正数")
    private Long spuId;

    @NotBlank(message = "SKU编码不能为空")
    @Size(max = 32, message = "SKU编码不能超过32个字符")
    private String skuCode;

    @Size(max = 2000, message = "规格JSON不能超过2000个字符")
    private String specJson;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.00", message = "价格不能小于0")
    private BigDecimal price;

    @DecimalMin(value = "0.00", message = "成本价不能小于0")
    private BigDecimal costPrice;

    @Size(max = 16, message = "单位不能超过16个字符")
    private String unit;

    @Size(max = 512, message = "图片地址不能超过512个字符")
    private String imageUrl;

    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    private Integer status;
}