package com.salemanager.modules.i18n.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "i18n")
public class I18nProperties {

    private String defaultLocale = "zh-CN";
    private List<String> supportedLocales = List.of("zh-CN", "en-US", "ja-JP");
    private Map<String, List<String>> fallbackChain = Map.of(
        "zh-TW", List.of("zh-CN", "en-US"),
        "ja-JP", List.of("en-US", "zh-CN"),
        "en-US", List.of("zh-CN")
    );
    private Cache cache = new Cache();

    @Data
    public static class Cache {
        private int unitTtl = 3600;
        private int entityTtl = 3600;
        private int statusTtl = 1800;
    }
}
