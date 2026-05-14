package com.salemanager.modules.product.param;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 商品模拟数据请求参数
 */
@Data
public class ProductMockDataParam {

    @Min(value = 1, message = "商品数量至少为1")
    @Max(value = 100, message = "商品数量不能超过100")
    private Integer goodsCount = 20;

    @Min(value = 1, message = "每个商品的SKU数量至少为1")
    @Max(value = 20, message = "每个商品的SKU数量不能超过20")
    private Integer skuPerGoods = 4;

    @Min(value = 1, message = "每个SKU的SN码数量至少为1")
    @Max(value = 50, message = "每个SKU的SN码数量不能超过50")
    private Integer snPerSku = 5;

    @Min(value = 1, message = "规格名称数量至少为1")
    @Max(value = 10, message = "规格名称数量不能超过10")
    private Integer specCount = 3;

    @Min(value = 2, message = "每个规格的规格值数量至少为2")
    @Max(value = 20, message = "每个规格的规格值数量不能超过20")
    private Integer valuesPerSpec = 6;
}