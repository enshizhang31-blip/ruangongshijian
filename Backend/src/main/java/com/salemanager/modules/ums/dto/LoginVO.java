package com.salemanager.modules.ums.dto;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginVO {

    private Long userId;
    private String username;
    private String realName;
    private String token;
    private String permissions;
    private String routes;
}