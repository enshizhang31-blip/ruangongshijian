package com.salemanager.modules.sale.param;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单请求参数
 */
@Data
public class SaleOrderParam {

    @NotNull(message = "客户ID不能为空")
    @Positive(message = "客户ID必须是正数")
    private Long customerId;

    @Size(max = 50, message = "客户姓名不能超过50个字符")
    private String customerName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String customerPhone;

    @NotEmpty(message = "订单商品不能为空")
    @Valid
    private List<OrderItemParam> items;

    @Min(value = 1, message = "支付方式无效")
    @Max(value = 4, message = "支付方式无效")
    private Integer payType;

    @Min(value = 0, message = "订单状态无效")
    @Max(value = 5, message = "订单状态无效")
    private Integer status;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;

    @Data
    public static class OrderItemParam {

        @NotNull(message = "SKU ID不能为空")
        @Positive(message = "SKU ID必须是正数")
        private Long skuId;

        @NotBlank(message = "商品名称不能为空")
        @Size(max = 200, message = "商品名称不能超过200个字符")
        private String spuName;

        @Size(max = 500, message = "规格信息不能超过500个字符")
        private String skuSpec;

        @Size(max = 500, message = "SKU图片不能超过500个字符")
        private String skuImage;

        @NotNull(message = "价格不能为空")
        @DecimalMin(value = "0.00", message = "价格不能为负数")
        @Digits(integer = 10, fraction = 2, message = "价格格式不正确")
        private BigDecimal price;

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量最少为1")
        @Max(value = 9999, message = "数量不能超过9999")
        private Integer quantity;
    }
}
