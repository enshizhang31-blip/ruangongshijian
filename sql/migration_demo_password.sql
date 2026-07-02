-- =============================================================
--  演示版迁移：新增 customer.password 字段
--  适用于已经有 customer 表但缺少 password 列的环境
-- =============================================================
ALTER TABLE customer
  ADD COLUMN IF NOT EXISTS password VARCHAR(128)
  COMMENT '登录密码(SHA-256 + 盐，演示版)'
  AFTER phone;
