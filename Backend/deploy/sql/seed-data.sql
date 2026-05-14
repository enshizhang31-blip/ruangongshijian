-- =====================================================
-- 销售管理系统 - 种子数据 (DML only)
-- 使用 INSERT IGNORE 防止重复执行报错
-- 手动执行: docker exec -i salemanager-mysql mysql -u salemanager -pSm@MySQL2026!User sale_manager < seed-data.sql
-- =====================================================

USE sale_manager;

-- 预设角色
INSERT IGNORE INTO role (id, name, code, description, permissions, routes, is_preset, status, created_at, updated_at) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '拥有所有权限', '["spu:view","spu:add","spu:edit","spu:delete","spu:import","spu:export","spu:status","sku:view","sku:add","sku:edit","sku:delete","category:view","category:add","category:edit","category:delete","spec:view","spec:add","spec:edit","spec:delete","sn:view","sn:add","sn:import","sn:export","sn:generate","sn:query","sn:status","customer:view","customer:detail","customer:edit","customer:balance","customer:points","customer:disable","order:view","order:detail","order:process","order:refund","statistics:view","system:user","system:role","system:menu","system:log"]', '["/dashboard","/product","/product/list","/sn","/sn/list","/order","/order/list","/customer","/customer/list","/statistics","/system","/system/user","/system/role","/i18n","/i18n/status"]', 1, 1, NOW(), NOW()),
(2, '运营主管', 'OPERATOR', '商品、订单、客户、数据统计管理', '["spu:view","spu:add","spu:edit","spu:delete","spu:import","spu:export","sku:view","sku:add","sku:edit","sku:delete","sn:view","sn:add","sn:import","sn:export","sn:query","category:view","category:add","category:edit","order:view","order:edit","customer:view","customer:add","customer:edit","statistics:view"]', '["/dashboard","/product","/sn","/order","/customer","/statistics"]', 1, 1, NOW(), NOW()),
(3, '录入员', 'INPUTTER', '商品新增、SN码查询、操作记录', '["spu:add","spu:edit","sn:add","sn:import","sn:query","sn:view","category:add","category:edit"]', '["/dashboard","/product","/sn"]', 1, 1, NOW(), NOW());

-- 预设管理员 (密码: 123456)
INSERT IGNORE INTO admin_user (id, username, password, real_name, permissions, routes, status, created_at, updated_at) VALUES
(1, 'admin', '$2a$10$9Fr2OrEUefhFyxvFEviIg.kHuXtbYVT44sQyFk9MSaAbDksT1Z.mK', '系统管理员', '["spu:view","spu:add","spu:edit","spu:delete","spu:import","spu:export","spu:status","sku:view","sku:add","sku:edit","sku:delete","category:view","category:add","category:edit","category:delete","spec:view","spec:add","spec:edit","spec:delete","sn:view","sn:add","sn:import","sn:export","sn:generate","sn:query","sn:status","customer:view","customer:detail","customer:edit","customer:balance","customer:points","customer:disable","order:view","order:detail","order:process","order:refund","statistics:view","system:user","system:role","system:menu","system:log"]', '["/dashboard","/product","/product/list","/sn","/sn/list","/order","/order/list","/customer","/customer/list","/statistics","/system","/system/user","/system/role","/i18n","/i18n/status"]', 1, NOW(), NOW());

-- 预设菜单
INSERT IGNORE INTO menu (id, name, path, component, icon, sort, parent_id, type, permission, status, created_at) VALUES
(1, '仪表盘', '/dashboard', 'dashboard/index', 'HomeIcon', 1, 0, 1, 'dashboard:view', 1, NOW()),
(2, '商品管理', '/product', NULL, 'CubeIcon', 2, 0, 1, 'product:view', 1, NOW()),
(3, '商品列表', '/product/list', 'product/ProductList', NULL, 1, 2, 1, 'spu:view', 1, NOW()),
(4, 'SN码管理', '/sn', NULL, 'TagIcon', 3, 0, 1, 'sn:view', 1, NOW()),
(5, 'SN码列表', '/sn/list', 'sn/SnList', NULL, 1, 4, 1, 'sn:view', 1, NOW()),
(6, '订单管理', '/order', NULL, 'ShoppingCartIcon', 4, 0, 1, 'order:view', 1, NOW()),
(7, '订单列表', '/order/list', 'sale/SaleOrderList', NULL, 1, 6, 1, 'order:view', 1, NOW()),
(8, '客户管理', '/customer', NULL, 'UsersIcon', 5, 0, 1, 'customer:view', 1, NOW()),
(9, '客户列表', '/customer/list', 'customer/CustomerList', NULL, 1, 8, 1, 'customer:view', 1, NOW()),
(10, '数据统计', '/statistics', NULL, 'ChartBarIcon', 6, 0, 1, 'statistics:view', 1, NOW()),
(11, '系统管理', '/system', NULL, 'Cog6ToothIcon', 100, 0, 1, 'system:view', 1, NOW()),
(12, '员工管理', '/system/user', 'system/UserList', NULL, 1, 11, 1, 'system:user', 1, NOW()),
(13, '角色管理', '/system/role', 'system/RoleList', NULL, 2, 11, 1, 'system:role', 1, NOW());