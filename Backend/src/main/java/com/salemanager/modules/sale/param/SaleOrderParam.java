package com.salemanager.modules.sale.param;

import lombok.Data;

import java.util.List;

/**
 * 订单请求参数
 */
@Data
public class SaleOrderParam {

    private Long customerId;
    private String customerName;
    private String customerPhone;
    private List<OrderItemParam> items;
    private Integer payType;
    private Integer status;
    private String remark;

    @Data
    public static class OrderItemParam {
        private Long skuId;
        private String spuName;
        private String skuSpec;
        private String skuImage;
        private Double price;
        private Integer quantity;
    }
}
