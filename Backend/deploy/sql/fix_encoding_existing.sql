-- =====================================================
-- 修复种子数据中可能存在的双重 UTF-8 编码
-- 由于历史遗留：MySQL Docker 初次初始化使用 latin1 客户端字符集，
-- UTF-8 编码的 SQL 文件被双重编码。本文件是幂等的，可以重复执行。
-- =====================================================

USE sale_manager;

-- role 表
UPDATE role SET name = '超级管理员' WHERE id = 1 AND name <> '超级管理员';
UPDATE role SET description = '拥有所有权限' WHERE id = 1 AND description <> '拥有所有权限';
UPDATE role SET name = '运营主管' WHERE id = 2 AND name <> '运营主管';
UPDATE role SET description = '商品、订单、客户、数据统计管理' WHERE id = 2 AND description <> '商品、订单、客户、数据统计管理';
UPDATE role SET name = '录入员' WHERE id = 3 AND name <> '录入员';
UPDATE role SET description = '商品新增、SN码查询、操作记录' WHERE id = 3 AND description <> '商品新增、SN码查询、操作记录';

-- menu 表
UPDATE menu SET name = '仪表盘'   WHERE id = 1  AND name <> '仪表盘';
UPDATE menu SET name = '商品管理' WHERE id = 2  AND name <> '商品管理';
UPDATE menu SET name = '商品列表' WHERE id = 3  AND name <> '商品列表';
UPDATE menu SET name = 'SN码管理' WHERE id = 4  AND name <> 'SN码管理';
UPDATE menu SET name = 'SN码列表' WHERE id = 5  AND name <> 'SN码列表';
UPDATE menu SET name = '订单管理' WHERE id = 6  AND name <> '订单管理';
UPDATE menu SET name = '订单列表' WHERE id = 7  AND name <> '订单列表';
UPDATE menu SET name = '客户管理' WHERE id = 8  AND name <> '客户管理';
UPDATE menu SET name = '客户列表' WHERE id = 9  AND name <> '客户列表';
UPDATE menu SET name = '数据统计' WHERE id = 10 AND name <> '数据统计';
UPDATE menu SET name = '系统管理' WHERE id = 11 AND name <> '系统管理';
UPDATE menu SET name = '员工管理' WHERE id = 12 AND name <> '员工管理';
UPDATE menu SET name = '角色管理' WHERE id = 13 AND name <> '角色管理';
