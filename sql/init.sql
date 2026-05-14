-- =====================================================
-- 销售管理系统 - Docker 自动初始化 (DDL only)
-- 此文件由 docker-compose 挂载到 /docker-entrypoint-initdb.d/
-- MySQL 首次启动时自动执行表结构初始化
-- 种子数据手动执行: source seed-data.sql
-- =====================================================

SOURCE /docker-entrypoint-initdb.d/schema.sql;
