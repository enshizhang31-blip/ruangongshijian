-- =====================================================
-- 销售管理系统 - 数据库初始化脚本
-- 数据库: MySQL 8.0+
-- 字符集: utf8mb4
-- =====================================================

SET NAMES utf8mb4;

-- =====================================================
-- 一、商品模块（SPU + SKU + SN码 + 分类 + 规格）
-- =====================================================

-- 1.1 商品分类表
CREATE TABLE IF NOT EXISTS goods_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(64) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
    icon VARCHAR(255) COMMENT '图标URL',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_parent (parent_id),
    INDEX idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 1.2 SPU商品表（标准产品单位）
CREATE TABLE IF NOT EXISTS goods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(128) NOT NULL COMMENT 'SPU名称',
    category_id BIGINT COMMENT '分类ID',
    brand VARCHAR(64) COMMENT '品牌',
    image_url VARCHAR(512) COMMENT '主图片URL',
    images TEXT COMMENT '多图片JSON数组',
    description TEXT COMMENT '商品描述',
    status TINYINT DEFAULT 0 COMMENT '状态: 0下架 1上架',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_category (category_id),
    INDEX idx_status (status),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SPU商品表';

-- 1.3 SKU规格表
CREATE TABLE IF NOT EXISTS sku (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    spu_id BIGINT NOT NULL COMMENT 'SPU ID',
    sku_code VARCHAR(32) NOT NULL COMMENT 'SKU编码',
    spec_json JSON COMMENT '规格JSON',
    price DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '销售价格',
    cost_price DECIMAL(10,2) COMMENT '成本价',
    unit VARCHAR(16) DEFAULT '件' COMMENT '单位',
    image_url VARCHAR(512) COMMENT 'SKU图片',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_sku_code (sku_code),
    INDEX idx_spu (spu_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SKU规格表';

-- 1.4 SN码表（商品实例）
CREATE TABLE IF NOT EXISTS sn_code (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    sn_code VARCHAR(64) NOT NULL COMMENT 'SN码',
    sku_id BIGINT COMMENT 'SKU ID',
    spu_id BIGINT COMMENT 'SPU ID',
    spu_name VARCHAR(128) COMMENT 'SPU名称',
    sku_code VARCHAR(32) COMMENT 'SKU编码',
    spec_json JSON COMMENT '规格JSON',
    price DECIMAL(10,2) COMMENT '录入时的价格',
    status TINYINT DEFAULT 0 COMMENT '状态: 0在库 1已售 2已作废 3退货中 4已退货',
    source TINYINT DEFAULT 1 COMMENT '来源: 1手动 2CSV 3自动生成',
    created_at DATETIME COMMENT '录入时间',
    sold_at DATETIME COMMENT '销售时间',
    updated_at DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_sn_code (sn_code),
    INDEX idx_sku (sku_id),
    INDEX idx_status (status),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SN码表';

-- 1.5 SN码操作日志表
CREATE TABLE IF NOT EXISTS sn_code_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    sn_code_id BIGINT COMMENT 'SN码ID',
    sn_code VARCHAR(64) COMMENT 'SN码',
    sku_id BIGINT COMMENT 'SKU ID',
    operation VARCHAR(32) NOT NULL COMMENT '操作类型',
    from_status TINYINT COMMENT '操作前状态',
    to_status TINYINT COMMENT '操作后状态',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(64) COMMENT '操作人姓名',
    remark VARCHAR(256) COMMENT '备注',
    created_at DATETIME COMMENT '操作时间',
    INDEX idx_sn_code_id (sn_code_id),
    INDEX idx_operation (operation),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SN码操作日志表';

-- 1.6 商品统计表
CREATE TABLE IF NOT EXISTS goods_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    goods_id BIGINT COMMENT '商品ID',
    date_type VARCHAR(16) COMMENT '统计周期: daily, monthly, yearly',
    stat_date DATE COMMENT '统计日期',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    order_count INT DEFAULT 0 COMMENT '下单次数',
    sale_count INT DEFAULT 0 COMMENT '销售数量',
    sale_amount DECIMAL(12,2) DEFAULT 0 COMMENT '销售金额',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_goods_date (goods_id, date_type, stat_date),
    INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品统计表';

-- 1.7 规格名称表
CREATE TABLE IF NOT EXISTS spec_name (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(64) NOT NULL COMMENT '规格名称',
    category_id BIGINT COMMENT '分类ID',
    sort INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规格名称表';

-- 1.8 规格值表
CREATE TABLE IF NOT EXISTS spec_value (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    spec_id BIGINT NOT NULL COMMENT '规格名称ID',
    value VARCHAR(128) NOT NULL COMMENT '规格值',
    sort INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_spec (spec_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规格值表';

-- =====================================================
-- 二、会员与客户模块
-- =====================================================

-- 2.1 会员等级配置表
CREATE TABLE IF NOT EXISTS member_level_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    level TINYINT NOT NULL UNIQUE COMMENT '等级值',
    name VARCHAR(32) NOT NULL COMMENT '等级名称',
    consume_threshold DECIMAL(12,2) COMMENT '升级门槛',
    discount DECIMAL(3,2) DEFAULT 1.00 COMMENT '折扣率',
    points_rate INT DEFAULT 1 COMMENT '积分倍率',
    status TINYINT DEFAULT 1 COMMENT '状态',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级配置表';

-- 2.2 客户表（会员）
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    openid VARCHAR(64) COMMENT '微信openid',
    nickname VARCHAR(64) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    phone VARCHAR(20) COMMENT '手机号',
    member_level TINYINT DEFAULT 1 COMMENT '会员等级：1普通 2银卡 3金卡 4钻石',
    balance DECIMAL(12,2) DEFAULT 0 COMMENT '账户余额',
    points INT DEFAULT 0 COMMENT '积分余额',
    total_consume DECIMAL(12,2) DEFAULT 0 COMMENT '累计消费金额',
    total_points INT DEFAULT 0 COMMENT '累计获得积分',
    status TINYINT DEFAULT 1 COMMENT '状态',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_openid (openid),
    UNIQUE KEY uk_phone (phone),
    INDEX idx_member_level (member_level),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- 2.3 收货地址表
CREATE TABLE IF NOT EXISTS address (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    receiver_name VARCHAR(64) NOT NULL COMMENT '收货人姓名',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    province VARCHAR(32) COMMENT '省份',
    city VARCHAR(32) COMMENT '城市',
    district VARCHAR(32) COMMENT '区县',
    detail VARCHAR(256) COMMENT '详细地址',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认: 0否 1是',
    status TINYINT DEFAULT 1 COMMENT '状态',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_customer (customer_id),
    INDEX idx_default (customer_id, is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- 2.4 余额变动记录表
CREATE TABLE IF NOT EXISTS balance_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    type TINYINT NOT NULL COMMENT '类型：1充值 2消费 3退款 4调整',
    amount DECIMAL(12,2) NOT NULL COMMENT '变动金额',
    balance_before DECIMAL(12,2) COMMENT '变动前余额',
    balance_after DECIMAL(12,2) COMMENT '变动后余额',
    source VARCHAR(32) COMMENT '来源',
    source_id BIGINT COMMENT '来源ID',
    payment_method VARCHAR(32) COMMENT '支付方式',
    remark VARCHAR(256) COMMENT '备注',
    created_at DATETIME COMMENT '创建时间',
    INDEX idx_customer (customer_id),
    INDEX idx_type (type),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='余额变动记录表';

-- 2.5 积分变动记录表
CREATE TABLE IF NOT EXISTS points_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    type TINYINT NOT NULL COMMENT '类型：1获得 2抵扣 3过期 4调整',
    amount INT NOT NULL COMMENT '积分数量',
    balance INT COMMENT '变动后余额',
    source VARCHAR(32) COMMENT '来源',
    source_id BIGINT COMMENT '来源ID',
    remark VARCHAR(256) COMMENT '备注',
    created_at DATETIME COMMENT '创建时间',
    INDEX idx_customer (customer_id),
    INDEX idx_type (type),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分变动记录表';

-- 2.6 用户行为日志表
CREATE TABLE IF NOT EXISTS user_behavior_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    customer_id BIGINT COMMENT '客户ID',
    session_id VARCHAR(64) COMMENT '会话ID',
    behavior_type VARCHAR(32) NOT NULL COMMENT '行为类型: view, click, search, add_cart, order',
    target_type VARCHAR(32) COMMENT '目标类型: goods, sku, category, search',
    target_id BIGINT COMMENT '目标ID',
    target_name VARCHAR(128) COMMENT '目标名称',
    search_keyword VARCHAR(128) COMMENT '搜索关键词',
    referer_url VARCHAR(512) COMMENT '来源URL',
    ip_address VARCHAR(64) COMMENT 'IP地址',
    user_agent TEXT COMMENT 'User Agent',
    created_at DATETIME COMMENT '行为时间',
    INDEX idx_customer (customer_id),
    INDEX idx_behavior (behavior_type),
    INDEX idx_target (target_type, target_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为日志表';

-- 2.7 页面统计表
CREATE TABLE IF NOT EXISTS page_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    page_url VARCHAR(256) NOT NULL COMMENT '页面URL',
    page_name VARCHAR(128) COMMENT '页面名称',
    stat_date DATE NOT NULL COMMENT '统计日期',
    pv INT DEFAULT 0 COMMENT '页面浏览量',
    uv INT DEFAULT 0 COMMENT '访问用户数',
    avg_stay_time INT DEFAULT 0 COMMENT '平均停留时长(秒)',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_page_date (page_url, stat_date),
    INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='页面统计表';

-- 2.8 用户留存统计表
CREATE TABLE IF NOT EXISTS user_retained_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    stat_date DATE NOT NULL COMMENT '统计日期',
    period_type VARCHAR(16) NOT NULL COMMENT '周期类型: daily, weekly, monthly',
    period_days INT COMMENT '周期天数',
    new_users INT DEFAULT 0 COMMENT '新增用户数',
    retained_users INT DEFAULT 0 COMMENT '留存用户数',
    retained_rate DECIMAL(5,2) DEFAULT 0 COMMENT '留存率',
    created_at DATETIME COMMENT '创建时间',
    INDEX idx_stat_date (stat_date),
    INDEX idx_period (period_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户留存统计表';

-- =====================================================
-- 三、购物车模块
-- =====================================================

-- 3.1 购物车表
CREATE TABLE IF NOT EXISTS cart (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    quantity INT DEFAULT 1 COMMENT '数量',
    selected TINYINT DEFAULT 1 COMMENT '是否选中: 0否 1是',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_customer_sku (customer_id, sku_id),
    INDEX idx_customer (customer_id),
    INDEX idx_sku (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- =====================================================
-- 四、订单模块
-- =====================================================

-- 4.1 订单表
CREATE TABLE IF NOT EXISTS `order` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(64) COMMENT '客户姓名',
    total_amount DECIMAL(12,2) NOT NULL COMMENT '订单总金额',
    discount_amount DECIMAL(12,2) DEFAULT 0 COMMENT '优惠金额',
    points_discount DECIMAL(12,2) DEFAULT 0 COMMENT '积分抵扣金额',
    pay_amount DECIMAL(12,2) NOT NULL COMMENT '实付金额',
    pay_type TINYINT COMMENT '支付方式: 1微信 2支付宝 3余额',
    pay_time DATETIME COMMENT '支付时间',
    status TINYINT DEFAULT 0 COMMENT '订单状态: 0待付款 1已付款 2已完成 3已取消 4退款中 5已退款',
    address_id BIGINT COMMENT '收货地址ID',
    receiver_name VARCHAR(64) COMMENT '收货人',
    receiver_phone VARCHAR(20) COMMENT '联系电话',
    receiver_address VARCHAR(256) COMMENT '收货地址',
    remark VARCHAR(256) COMMENT '订单备注',
    cancel_time DATETIME COMMENT '取消时间',
    finish_time DATETIME COMMENT '完成时间',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_customer (customer_id),
    INDEX idx_status (status),
    INDEX idx_created (created_at),
    INDEX idx_pay_time (pay_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 4.2 订单明细表
CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_no VARCHAR(32) COMMENT '订单编号（冗余）',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    spu_name VARCHAR(128) COMMENT '商品名称',
    sku_spec JSON COMMENT '规格JSON',
    sku_image VARCHAR(255) COMMENT 'SKU图片',
    price DECIMAL(10,2) NOT NULL COMMENT '购买单价',
    quantity INT NOT NULL COMMENT '购买数量',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    sn_code_ids VARCHAR(512) COMMENT '关联SN码ID列表JSON',
    created_at DATETIME COMMENT '创建时间',
    INDEX idx_order (order_id),
    INDEX idx_sku (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 4.3 退款订单表
CREATE TABLE IF NOT EXISTS refund_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    refund_no VARCHAR(32) NOT NULL COMMENT '退款单号',
    order_id BIGINT NOT NULL COMMENT '原订单ID',
    order_no VARCHAR(32) COMMENT '原订单号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    refund_type TINYINT NOT NULL COMMENT '退款类型: 1仅退款 2退货退款',
    refund_reason VARCHAR(256) COMMENT '退款原因',
    refund_amount DECIMAL(12,2) NOT NULL COMMENT '退款金额',
    points_refund INT DEFAULT 0 COMMENT '退还积分',
    status TINYINT DEFAULT 0 COMMENT '退款状态: 0待处理 1处理中 2已完成 3已拒绝',
    reject_reason VARCHAR(256) COMMENT '拒绝原因',
    express_company VARCHAR(64) COMMENT '快递公司',
    express_no VARCHAR(64) COMMENT '快递单号',
    apply_time DATETIME COMMENT '申请时间',
    process_time DATETIME COMMENT '处理时间',
    completed_time DATETIME COMMENT '完成时间',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_refund_no (refund_no),
    INDEX idx_order (order_id),
    INDEX idx_customer (customer_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款订单表';

-- =====================================================
-- 五、系统管理模块（员工、角色、菜单）
-- =====================================================

-- 5.1 部门表
CREATE TABLE IF NOT EXISTS department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(64) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_parent (parent_id),
    INDEX idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 5.2 员工表
CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(64) NOT NULL COMMENT '用户名',
    password VARCHAR(128) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name VARCHAR(64) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(128) COMMENT '邮箱',
    permissions JSON COMMENT '权限数组(创建时从角色复制,之后独立)',
    routes JSON COMMENT '路由数组(创建时从角色复制,之后独立)',
    department_id BIGINT COMMENT '部门ID',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    last_login_at DATETIME COMMENT '最后登录时间',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_username (username),
    KEY idx_username (username),
    INDEX idx_department (department_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- 5.3 角色表
CREATE TABLE IF NOT EXISTS role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(64) NOT NULL COMMENT '角色名称',
    code VARCHAR(32) NOT NULL COMMENT '角色代码',
    description VARCHAR(256) COMMENT '角色描述',
    permissions JSON COMMENT '权限数组',
    routes JSON COMMENT '路由数组',
    is_preset TINYINT DEFAULT 0 COMMENT '是否预设: 0否 1是',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_code (code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 5.4 菜单表
CREATE TABLE IF NOT EXISTS menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(64) NOT NULL COMMENT '菜单名称',
    path VARCHAR(128) COMMENT '路由路径',
    component VARCHAR(256) COMMENT '组件路径',
    icon VARCHAR(64) COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
    type TINYINT DEFAULT 1 COMMENT '类型: 1菜单 2按钮 3接口',
    permission VARCHAR(64) COMMENT '权限标识',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_parent (parent_id),
    INDEX idx_type (type),
    INDEX idx_permission (permission),
    INDEX idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 5.5 操作日志表
CREATE TABLE IF NOT EXISTS admin_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(64) COMMENT '操作人姓名',
    operation VARCHAR(32) NOT NULL COMMENT '操作类型',
    target_type VARCHAR(32) COMMENT '目标类型',
    target_id BIGINT COMMENT '目标ID',
    target_name VARCHAR(128) COMMENT '目标名称',
    ip_address VARCHAR(64) COMMENT 'IP地址',
    user_agent TEXT COMMENT 'User Agent',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(256) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    response_status INT COMMENT '响应状态',
    error_message TEXT COMMENT '错误信息',
    execution_time INT COMMENT '执行时长(ms)',
    created_at DATETIME COMMENT '操作时间',
    INDEX idx_operator (operator_id),
    INDEX idx_operation (operation),
    INDEX idx_target (target_type, target_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- =====================================================
-- 六、初始化数据
-- =====================================================

-- 6.1 预设角色
INSERT INTO role (name, code, description, permissions, routes, is_preset, status, created_at, updated_at) VALUES
('超级管理员', 'SUPER_ADMIN', '拥有所有权限', '["*"]', '["/*"]', 1, 1, NOW(), NOW()),
('运营主管', 'OPERATOR', '商品、订单、客户、数据统计管理', '["spu:view","spu:add","spu:edit","spu:delete","spu:import","spu:export","sku:view","sku:add","sku:edit","sku:delete","sn:view","sn:add","sn:import","sn:export","sn:query","category:view","category:add","category:edit","order:view","order:edit","customer:view","customer:add","customer:edit","statistics:view"]', '["/dashboard","/product","/sn","/order","/customer","/statistics"]', 1, 1, NOW(), NOW()),
('录入员', 'INPUTTER', '商品新增、SN码查询、操作记录', '["spu:add","spu:edit","sn:add","sn:import","sn:query","sn:view","category:add","category:edit"]', '["/dashboard","/product","/sn"]', 1, 1, NOW(), NOW());

-- 6.2 预设管理员账户
-- 用户名: admin  密码: 123456 (BCrypt hash)
INSERT INTO admin_user (username, password, real_name, permissions, routes, status, created_at, updated_at) VALUES
('admin', '$2a$10$9Fr2OrEUefhFyxvFEviIg.kHuXtbYVT44sQyFk9MSaAbDksT1Z.mK', '系统管理员', '["*"]', '["/*"]', 1, NOW(), NOW());

-- 6.3 预设菜单数据
INSERT INTO menu (name, path, component, icon, sort, parent_id, type, permission, status, created_at, updated_at) VALUES
('仪表盘', '/dashboard', 'dashboard/index', 'HomeIcon', 1, 0, 1, 'dashboard:view', 1, NOW(), NOW()),
('商品管理', '/product', NULL, 'CubeIcon', 2, 0, 1, 'product:view', 1, NOW(), NOW()),
('商品列表', '/product/list', 'product/ProductList', NULL, 1, 2, 1, 'spu:view', 1, NOW(), NOW()),
('SN码管理', '/sn', NULL, 'TagIcon', 3, 0, 1, 'sn:view', 1, NOW(), NOW()),
('SN码列表', '/sn/list', 'sn/SnList', NULL, 1, 4, 1, 'sn:view', 1, NOW(), NOW()),
('订单管理', '/order', NULL, 'ShoppingCartIcon', 4, 0, 1, 'order:view', 1, NOW(), NOW()),
('订单列表', '/order/list', 'sale/SaleOrderList', NULL, 1, 6, 1, 'order:view', 1, NOW(), NOW()),
('客户管理', '/customer', NULL, 'UsersIcon', 5, 0, 1, 'customer:view', 1, NOW(), NOW()),
('客户列表', '/customer/list', 'customer/CustomerList', NULL, 1, 8, 1, 'customer:view', 1, NOW(), NOW()),
('数据统计', '/statistics', NULL, 'ChartBarIcon', 6, 0, 1, 'statistics:view', 1, NOW(), NOW()),
('系统管理', '/system', NULL, 'Cog6ToothIcon', 100, 0, 1, 'system:view', 1, NOW(), NOW()),
('员工管理', '/system/user', 'system/UserList', NULL, 1, 11, 1, 'system:user', 1, NOW(), NOW()),
('角色管理', '/system/role', 'system/RoleList', NULL, 2, 11, 1, 'system:role', 1, NOW(), NOW());
