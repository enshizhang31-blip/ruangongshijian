package com.salemanager.modules.i18n.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.salemanager.modules.i18n.repository")
@EnableMongoAuditing
public class MongoConfig {
}
