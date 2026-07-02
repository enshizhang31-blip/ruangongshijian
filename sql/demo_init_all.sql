-- =============================================================
--  SaleManager 演示版一站式初始化脚本
--
--  适合场景：
--    1. 空库全新建数据库
--    2. 已有库但缺少 password 列（旧版 schema 没加）
--    3. 演示启动前希望保证 customer 表结构正确
--
--  执行方式：
--    mysql -uroot -p your_database < demo_init_all.sql
--
--  会做的事：
--    1. 演示版新增字段 customer.password（幂等）
--    2. 确保演示账号后端可通过 @PostConstruct 自动 seed（无需手动插入）
-- =============================================================

-- ----- 1. 演示版新增字段（幂等 ALTER） -----
ALTER TABLE customer
  ADD COLUMN IF NOT EXISTS password VARCHAR(128)
  COMMENT '登录密码(SHA-256 + 盐，演示版)'
  AFTER phone;

-- ----- 2. （可选）演示账号 DBA 侧手工插入 -----
-- 后端 @PostConstruct 已经会在启动时自动 seed demo/123456，
-- 下面这段只是 DBA 手工兜底，正常情况下无需执行：
--
-- INSERT IGNORE INTO customer (openid, nickname, phone, password, member_level, balance, points, total_consume, total_points, status, created_at, updated_at)
-- VALUES (
--   NULL,
--   '演示账号',
--   'demo',
--   -- SHA-256("demo-uniapp-salt" + "123456")
--   'bca1eddf80c5ff387c585934eba25f4dec6a6ca2e7ce5d50ce0d5f2cc12c7d39',
--   4,
--   9999.00,
--   9999,
--   5000.00,
--   9999,
--   1,
--   NOW(),
--   NOW()
-- );
