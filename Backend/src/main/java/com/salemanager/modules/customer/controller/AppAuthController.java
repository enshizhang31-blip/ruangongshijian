package com.salemanager.modules.customer.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.customer.service.AppAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AppAuthController {

    private static final Logger log = LoggerFactory.getLogger(AppAuthController.class);

    @Autowired
    private AppAuthService appAuthService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        log.info("wechatLogin code={}", code);
        return Result.success(appAuthService.wechatLogin(code));
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String password = body.get("password");
        String nickname = body.get("nickname");
        log.info("demo register phone={}", phone);
        return Result.success(appAuthService.registerByPhone(phone, password, nickname));
    }

    @PostMapping("/login-by-phone")
    public Result<Map<String, Object>> loginByPhone(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String password = body.get("password");
        log.info("demo login phone={}", phone);
        return Result.success(appAuthService.loginByPhone(phone, password));
    }
}
