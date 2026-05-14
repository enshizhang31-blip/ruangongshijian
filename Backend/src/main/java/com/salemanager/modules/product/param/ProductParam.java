package com.salemanager.modules.product.param;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 商品请求参数
 */
@Data
public class ProductParam {

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 200, message = "商品名称不能超过200个字符")
    private String name;

    @NotNull(message = "分类ID不能为空")
    @Positive(message = "分类ID必须是正数")
    private Long categoryId;

    @Size(max = 100, message = "品牌不能超过100个字符")
    private String brand;

    @Size(max = 500, message = "主图URL不能超过500个字符")
    private String imageUrl;

    @Size(max = 2000, message = "图片列表不能超过2000个字符")
    private String images;

    @Size(max = 256, message = "短描述不能超过256个字符")
    private String shortDesc;

    @Size(max = 5000, message = "商品描述不能超过5000个字符")
    private String description;

    @Min(value = 0, message = "状态值无效")
    @Max(value = 3, message = "状态值无效")
    private Integer status;
}
