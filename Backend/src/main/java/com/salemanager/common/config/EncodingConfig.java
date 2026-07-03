package com.salemanager.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 全局字符编码配置
 *
 * <p>双重保险：
 * <ol>
 *   <li>application.yml 已配置 server.servlet.encoding</li>
 *   <li>此类显式注册 UTF-8 转换器，确保 JSON 响应一律 UTF-8</li>
 * </ol>
 *
 * <p>解决管理端新增员工时角色下拉框中文乱码问题（Tomcat 默认 ISO-8859-1 改为 UTF-8）。
 */
@Configuration
public class EncodingConfig {

    /**
     * 强制 String 类型响应使用 UTF-8
     */
    @Bean
    public StringHttpMessageConverter stringHttpMessageConverter() {
        return new StringHttpMessageConverter(StandardCharsets.UTF_8);
    }
}
