package com.salemanager.modules.customer.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.i18n.config.I18nProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 小程序端公开的语言列表接口。
 *
 * 多语言「上线/下线」由后端控制：
 *   - 修改 application.yml 的 i18n.supported-locales 列表即可下线某种语言
 *   - 修改 i18n.default-locale 切换默认语言
 *
 * 演示场景：通过 yaml 控制前端可见的语言集合，无需改前端代码。
 */
@RestController
@RequestMapping("/api/i18n")
public class I18nPublicController {

    @Autowired
    private I18nProperties i18nProperties;

    @GetMapping("/locales")
    public Result<Map<String, Object>> locales() {
        Map<String, String> displayNames = Map.of(
            "zh-CN", "简体中文",
            "zh-TW", "繁體中文",
            "en-US", "English",
            "ja-JP", "日本語",
            "ko-KR", "한국어",
            "fr-FR", "Français",
            "de-DE", "Deutsch",
            "es-ES", "Español"
        );

        List<Map<String, Object>> list = new ArrayList<>();
        if (i18nProperties.getSupportedLocales() != null) {
            for (String code : i18nProperties.getSupportedLocales()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("code", code);
                item.put("name", displayNames.getOrDefault(code, code));
                item.put("enabled", true);
                list.add(item);
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("default", i18nProperties.getDefaultLocale());
        data.put("locales", list);
        return Result.success(data);
    }
}
