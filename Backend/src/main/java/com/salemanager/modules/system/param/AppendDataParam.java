package com.salemanager.modules.system.param;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AppendDataParam {

    @NotBlank(message = "模块不能为空")
    private String module;

    @Min(value = 1, message = "数量至少为1")
    @Max(value = 200, message = "数量不能超过200")
    private Integer count = 10;
}
