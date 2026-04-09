package com.salemanager.modules.customer.param;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 客户请求参数
 */
@Data
public class CustomerParam {

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(max = 64, message = "昵称不能超过64个字符")
    private String nickname;

    @Size(max = 255, message = "头像URL不能超过255个字符")
    private String avatar;

    @Min(value = 1, message = "会员等级值无效")
    @Max(value = 4, message = "会员等级值无效")
    private Integer memberLevel;

    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    private Integer status;
}
