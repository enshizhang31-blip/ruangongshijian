package com.salemanager.modules.customer.service;

import java.util.Map;

public interface AppAuthService {

    /** 微信登录 */
    Map<String, Object> wechatLogin(String code);

    /** 演示版：手机号 + 密码注册 */
    Map<String, Object> registerByPhone(String phone, String password, String nickname);

    /** 演示版：手机号 + 密码登录 */
    Map<String, Object> loginByPhone(String phone, String password);
}
