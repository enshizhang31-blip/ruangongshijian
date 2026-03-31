package com.salemanager.modules.ums.param;

import lombok.Data;

/**
 * 员工请求参数
 */
@Data
public class AdminUserParam {

    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private Long roleId;
    private Long departmentId;
    private Integer status;
}