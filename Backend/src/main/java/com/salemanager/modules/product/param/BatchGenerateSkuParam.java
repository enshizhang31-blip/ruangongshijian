package com.salemanager.modules.product.param;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 批量生成SKU请求参数
 */
@Data
public class BatchGenerateSkuParam {

    @NotNull(message = "SPU ID不能为空")
    @Positive(message = "SPU ID必须是正数")
    private Long spuId;

    /** 规格ID列表，系统对这些规格值做笛卡尔积 */
    @NotEmpty(message = "规格ID列表不能为空")
    private List<Long> specIds;

    /** SKU编码前缀，如 "IP15" */
    private String codePrefix;

    /** 默认价格（所有生成的SKU统一设此价格，之后可修改） */
    private BigDecimal defaultPrice;

    /** 默认成本价 */
    private BigDecimal defaultCostPrice;
}
