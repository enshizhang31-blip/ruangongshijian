package com.salemanager.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 启动时检查并修复数据库结构（幂等）。
 * 主要用于演示环境临时补齐缺失列，避免线上 500。
 */
@Component
@Order(0)
public class SchemaRepairRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaRepairRunner.class);

    private final DataSource dataSource;

    public SchemaRepairRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        ensureCustomerPasswordColumn();
    }

    private void ensureCustomerPasswordColumn() {
        try (Connection conn = dataSource.getConnection()) {
            if (columnExists(conn, "customer", "password")) {
                return;
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "ALTER TABLE customer ADD COLUMN password VARCHAR(128) " +
                    "COMMENT '密码（演示版 SHA-256(原始密码+盐)）' AFTER phone");
                log.warn("[SchemaRepairRunner] 已为 customer 表补齐 password 列");
            }
        } catch (Exception e) {
            log.error("[SchemaRepairRunner] 修复 customer 表结构失败", e);
        }
    }

    private boolean columnExists(Connection conn, String tableName, String columnName) {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, columnName)) {
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }
}