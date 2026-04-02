package com.salemanager.modules.product.param;

import lombok.Data;

/**
 * 商品请求参数
 */
@Data
public class ProductParam {

    private String name;
    private Long categoryId;
    private String brand;
    private String imageUrl;
    private String images;
    private String description;
    private Integer status;
}
