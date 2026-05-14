package com.salemanager.modules.i18n.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Data
public class SaveUnitParam {

    @NotBlank(message = "语言标识不能为空")
    @Size(max = 10)
    private String locale;

    /** 翻译值，类型由 field_type 决定 */
    @NotBlank(message = "翻译值不能为空")
    private Object value;

    /** 是否强制覆盖已审批的翻译 */
    private Boolean force = false;
}
