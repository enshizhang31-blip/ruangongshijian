package com.salemanager.modules.sn.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * SN码请求参数
 */
@Data
public class SnCodeParam {

    @Size(max = 100, message = "SN码不能超过100个字符")
    private String sn;

    private String[] sns;

    @Positive(message = "商品ID必须是正数")
    private Long goodsId;

    @Positive(message = "SKU ID必须是正数")
    private Long skuId;

    @Size(max = 500, message = "备注不能超过500个字符")
    private String remark;
}
