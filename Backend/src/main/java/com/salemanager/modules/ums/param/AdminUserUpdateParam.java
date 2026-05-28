package com.salemanager.modules.ums.param;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 员工更新请求参数（编辑时不需要用户名、密码、角色）
 */
@Data
public class AdminUserUpdateParam {

    @Size(max = 50, message = "真实姓名不能超过50个字符")
    private String realName;

    private String phone;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱不能超过100个字符")
    private String email;

    private Long departmentId;

    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    private Integer status;
}
