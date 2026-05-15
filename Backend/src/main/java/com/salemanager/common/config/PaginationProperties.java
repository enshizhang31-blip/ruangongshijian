package com.salemanager.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "sale-manager.pagination")
public class PaginationProperties {

    private int maxPageSize = 100;
}
