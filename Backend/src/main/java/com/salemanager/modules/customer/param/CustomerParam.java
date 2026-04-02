package com.salemanager.modules.customer.param;

import lombok.Data;

/**
 * 客户请求参数
 */
@Data
public class CustomerParam {

    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatarUrl;
    private Integer gender;
    private Integer status;
}
