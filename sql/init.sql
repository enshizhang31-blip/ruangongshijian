-- =====================================================
-- 销售管理系统 - 数据库初始化脚本
-- 数据库: MySQL 8.0+
-- 创建时间: 2026-03-29
--
-- 关联文档:
--   - doc/2026-03-28-数据库设计.md (表结构定义)
--   - doc/2026-03-27-需求详细设计.md (功能需求)
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS sale_manager DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sale_manager;

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
    INDEX idx_sn_code (sn_code_id),
    INDEX idx_operator (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SN码操作日志表';

-- 1.6 规格名称表
CREATE TABLE IF NOT EXISTS spec_name (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(32) NOT NULL COMMENT '规格名称',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    INDEX idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规格名称表';

-- 1.7 规格值表
CREATE TABLE IF NOT EXISTS spec_value (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    spec_id BIGINT NOT NULL COMMENT '规格名称ID',
    value VARCHAR(32) NOT NULL COMMENT '规格值',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    INDEX idx_spec (spec_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规格值表';

-- =====================================================
-- 二、客户/会员模块
-- =====================================================

-- 2.1 客户/会员表
CREATE TABLE IF NOT EXISTS customer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    openid VARCHAR(64) UNIQUE COMMENT '微信openid',
    nickname VARCHAR(64) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    phone VARCHAR(20) COMMENT '手机号',
    member_level TINYINT DEFAULT 1 COMMENT '会员等级: 1普通 2银卡 3金卡 4钻石',
    balance DECIMAL(10,2) DEFAULT 0 COMMENT '账户余额',
    points INT DEFAULT 0 COMMENT '积分余额',
    total_consume DECIMAL(10,2) DEFAULT 0 COMMENT '累计消费金额',
    total_points INT DEFAULT 0 COMMENT '累计获得积分',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    created_at DATETIME COMMENT '注册时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_openid (openid),
    INDEX idx_phone (phone),
    INDEX idx_level (member_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户/会员表';

-- 2.2 会员等级配置表
CREATE TABLE IF NOT EXISTS member_level_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    level TINYINT NOT NULL UNIQUE COMMENT '等级值',
    name VARCHAR(32) NOT NULL COMMENT '等级名称',
    consume_threshold DECIMAL(10,2) COMMENT '升级门槛',
    discount DECIMAL(3,2) DEFAULT 1.00 COMMENT '折扣率',
    points_rate INT DEFAULT 1 COMMENT '积分倍率',
    status TINYINT DEFAULT 1 COMMENT '状态',
    created_at DATETIME COMMENT '创建时间',
    UNIQUE KEY uk_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级配置表';

-- 2.3 积分记录表
CREATE TABLE IF NOT EXISTS points_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    customer_id BIGINT NOT NULL COMMENT '会员ID',
    type TINYINT COMMENT '类型: 1获得 2抵扣 3过期 4调整',
    amount INT NOT NULL COMMENT '积分数量',
    balance INT COMMENT '变动后余额',
    source VARCHAR(32) COMMENT '来源',
    source_id BIGINT COMMENT '来源ID',
    remark VARCHAR(256) COMMENT '备注',
    created_at DATETIME COMMENT '创建时间',
    INDEX idx_customer (customer_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分记录表';

-- 2.4 余额记录表
CREATE TABLE IF NOT EXISTS balance_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    customer_id BIGINT NOT NULL COMMENT '会员ID',
    type TINYINT COMMENT '类型: 1充值 2消费 3退款 4调整',
    amount DECIMAL(10,2) NOT NULL COMMENT '变动金额',
    balance DECIMAL(10,2) COMMENT '变动后余额',
    source VARCHAR(32) COMMENT '来源',
    source_id BIGINT COMMENT '来源ID',
    payment_method VARCHAR(32) COMMENT '支付方式',
    remark VARCHAR(256) COMMENT '备注',
    created_at DATETIME COMMENT '创建时间',
    INDEX idx_customer (customer_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='余额记录表';

-- 2.5 购物车表
CREATE TABLE IF NOT EXISTS cart (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    customer_id BIGINT NOT NULL COMMENT '会员ID',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    created_at DATETIME COMMENT '添加时间',
    updated_at DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_customer_sku (customer_id, sku_id),
    INDEX idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 2.6 收货地址表
CREATE TABLE IF NOT EXISTS address (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    customer_id BIGINT NOT NULL COMMENT '会员ID',
    receiver_name VARCHAR(64) NOT NULL COMMENT '收货人姓名',
    phone VARCHAR(20) NOT NULL COMMENT '联系电话',
    province VARCHAR(32) COMMENT '省份',
    city VARCHAR(32) COMMENT '城市',
    district VARCHAR(32) COMMENT '区/县',
    detail VARCHAR(256) COMMENT '详细地址',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认: 0否 1是',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- =====================================================
-- 三、订单模块
-- =====================================================

-- 3.1 订单表
CREATE TABLE IF NOT EXISTS `order` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单编号',
    customer_id BIGINT NOT NULL COMMENT '会员ID',
    customer_name VARCHAR(64) COMMENT '会员姓名',
    customer_phone VARCHAR(20) COMMENT '会员手机',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '商品总价',
    discount_amount DECIMAL(10,2) DEFAULT 0 COMMENT '优惠金额',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    pay_type TINYINT DEFAULT 1 COMMENT '支付方式: 1余额支付',
    status TINYINT DEFAULT 0 COMMENT '订单状态: 0待付款 1已付款 2已完成 3已取消 4退款中 5已退款',
    remark VARCHAR(256) COMMENT '订单备注',
    created_at DATETIME COMMENT '创建时间',
    paid_at DATETIME COMMENT '支付时间',
    completed_at DATETIME COMMENT '完成时间',
    cancelled_at DATETIME COMMENT '取消时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_customer (customer_id),
    INDEX idx_status (status),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 3.2 订单明细表
CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_no VARCHAR(32) COMMENT '订单编号',
    sku_id BIGINT NOT NULL COMMENT 'SKU ID',
    spu_name VARCHAR(128) COMMENT '商品名称',
    sku_spec JSON COMMENT '规格JSON',
    sku_image VARCHAR(255) COMMENT 'SKU图片',
    price DECIMAL(10,2) COMMENT '购买单价',
    quantity INT NOT NULL COMMENT '购买数量',
    subtotal DECIMAL(10,2) COMMENT '小计金额',
    sn_code_ids VARCHAR(512) COMMENT '关联SN码ID列表JSON',
    created_at DATETIME COMMENT '创建时间',
    INDEX idx_order (order_id),
    INDEX idx_sku (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 3.3 退款记录表
CREATE TABLE IF NOT EXISTS refund_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_id BIGINT COMMENT '订单ID',
    order_no VARCHAR(32) COMMENT '订单编号',
    customer_id BIGINT COMMENT '会员ID',
    refund_amount DECIMAL(10,2) NOT NULL COMMENT '退款金额',
    refund_points INT DEFAULT 0 COMMENT '退回积分',
    reason VARCHAR(256) COMMENT '退款原因',
    status TINYINT DEFAULT 0 COMMENT '审核状态: 0待审核 1同意 2拒绝',
    reject_reason VARCHAR(256) COMMENT '拒绝原因',
    operator_id BIGINT COMMENT '审核人ID',
    operator_name VARCHAR(64) COMMENT '审核人姓名',
    created_at DATETIME COMMENT '申请时间',
    processed_at DATETIME COMMENT '审核时间',
    INDEX idx_order (order_id),
    INDEX idx_customer (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款记录表';

-- =====================================================
-- 四、权限模块（管理端）
-- =====================================================

-- 4.1 员工表
CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(128) NOT NULL COMMENT '密码(BCrypt加密)',
    real_name VARCHAR(64) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(128) COMMENT '邮箱',
    role_id BIGINT COMMENT '角色ID',
    department_id BIGINT COMMENT '部门ID',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    last_login_at DATETIME COMMENT '最后登录时间',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工表';

-- 4.2 角色表
CREATE TABLE IF NOT EXISTS role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(64) NOT NULL UNIQUE COMMENT '角色名称',
    code VARCHAR(32) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(256) COMMENT '角色描述',
    permissions JSON COMMENT '权限数组',
    routes JSON COMMENT '路由数组',
    is_preset TINYINT DEFAULT 0 COMMENT '是否预设: 1预设 0自定义',
    status TINYINT DEFAULT 1 COMMENT '状态',
    created_at DATETIME COMMENT '创建时间',
    updated_at DATETIME COMMENT '更新时间',
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 4.3 菜单表
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
    status TINYINT DEFAULT 1 COMMENT '状态',
    created_at DATETIME COMMENT '创建时间',
    INDEX idx_parent (parent_id),
    INDEX idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 4.4 操作日志表
CREATE TABLE IF NOT EXISTS admin_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    admin_id BIGINT COMMENT '操作人ID',
    admin_name VARCHAR(64) COMMENT '操作人姓名',
    module VARCHAR(32) COMMENT '模块',
    operation VARCHAR(64) COMMENT '操作',
    target_id BIGINT COMMENT '目标ID',
    target_desc VARCHAR(256) COMMENT '目标描述',
    request_method VARCHAR(16) COMMENT '请求方法',
    request_url VARCHAR(256) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    ip VARCHAR(64) COMMENT 'IP地址',
    status TINYINT COMMENT '结果',
    error_msg VARCHAR(512) COMMENT '错误信息',
    created_at DATETIME COMMENT '操作时间',
    INDEX idx_admin (admin_id),
    INDEX idx_module (module),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 4.5 部门表
CREATE TABLE IF NOT EXISTS department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    name VARCHAR(64) NOT NULL COMMENT '部门名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    created_at DATETIME COMMENT '创建时间',
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- =====================================================
-- 五、小程序埋点模块（用户行为分析）
-- =====================================================

-- 5.1 用户行为日志表
CREATE TABLE IF NOT EXISTS user_behavior_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    customer_id BIGINT COMMENT '会员ID',
    openid VARCHAR(64) COMMENT '微信openid',
    event_type VARCHAR(32) NOT NULL COMMENT '事件类型',
    event_name VARCHAR(64) NOT NULL COMMENT '事件名称',
    page_url VARCHAR(256) COMMENT '页面路径',
    page_title VARCHAR(128) COMMENT '页面标题',
    duration INT COMMENT '停留时长(秒)',
    referrer VARCHAR(256) COMMENT '上一个页面',
    platform VARCHAR(16) COMMENT '平台: mini_program',
    device_id VARCHAR(64) COMMENT '设备ID',
    os_version VARCHAR(32) COMMENT '操作系统版本',
    app_version VARCHAR(16) COMMENT '小程序版本',
    network_type VARCHAR(16) COMMENT '网络类型',
    screen_width INT COMMENT '屏幕宽度',
    screen_height INT COMMENT '屏幕高度',
    extra_data JSON COMMENT '扩展数据',
    created_at DATETIME COMMENT '事件时间',
    INDEX idx_customer (customer_id),
    INDEX idx_event (event_type, event_name),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为日志表';

-- 5.2 用户留存统计表
CREATE TABLE IF NOT EXISTS user_retained_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    stat_date DATE NOT NULL COMMENT '统计日期',
    platform VARCHAR(16) NOT NULL COMMENT '平台',
    new_users INT DEFAULT 0 COMMENT '新增用户',
    active_users INT DEFAULT 0 COMMENT '活跃用户',
    retained_users_1d INT DEFAULT 0 COMMENT '次日留存',
    retained_users_3d INT DEFAULT 0 COMMENT '3日留存',
    retained_users_7d INT DEFAULT 0 COMMENT '7日留存',
    retained_users_30d INT DEFAULT 0 COMMENT '30日留存',
    created_at DATETIME COMMENT '创建时间',
    UNIQUE KEY uk_date_platform (stat_date, platform)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户留存统计表';

-- 5.3 页面访问统计表
CREATE TABLE IF NOT EXISTS page_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    stat_date DATE NOT NULL COMMENT '统计日期',
    page_url VARCHAR(256) NOT NULL COMMENT '页面路径',
    page_title VARCHAR(128) COMMENT '页面标题',
    pv INT DEFAULT 0 COMMENT '页面PV',
    uv INT DEFAULT 0 COMMENT '页面UV',
    avg_duration DECIMAL(10,2) DEFAULT 0 COMMENT '平均停留时长(秒)',
    created_at DATETIME COMMENT '创建时间',
    UNIQUE KEY uk_date_page (stat_date, page_url)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='页面访问统计表';

-- 5.4 商品访问统计表
CREATE TABLE IF NOT EXISTS goods_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    stat_date DATE NOT NULL COMMENT '统计日期',
    goods_id BIGINT NOT NULL COMMENT '商品ID(SPU ID)',
    pv INT DEFAULT 0 COMMENT '访问次数',
    uv INT DEFAULT 0 COMMENT '访问人数',
    cart_count INT DEFAULT 0 COMMENT '加购次数',
    order_count INT DEFAULT 0 COMMENT '下单次数',
    created_at DATETIME COMMENT '创建时间',
    UNIQUE KEY uk_date_goods (stat_date, goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品访问统计表';

-- =====================================================
-- 六、预设数据
-- =====================================================

-- 6.1 预设角色
INSERT INTO role (id, name, code, description, permissions, routes, is_preset, status, created_at, updated_at) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '拥有所有权限',
 '["*"]', '["/dashboard", "/product", "/sn", "/customer", "/order", "/statistics", "/system"]',
 1, 1, NOW(), NOW()),
(2, '运营主管', 'OPERATOR', '商品/订单/客户/统计管理',
 '["spu:*", "sku:*", "category:*", "spec:*", "sn:*", "customer:*", "order:*", "statistics:*"]',
 '["/dashboard", "/product", "/sn", "/customer", "/order", "/statistics"]',
 1, 1, NOW(), NOW()),
(3, '录入员', 'INPUTTER', '商品录入/SN码操作',
 '["spu:add", "spu:edit", "spu:view", "sku:add", "sku:edit", "sku:view", "sn:add", "sn:import", "sn:view", "sn:query"]',
 '["/dashboard", "/product", "/sn"]',
 1, 1, NOW(), NOW());

-- 6.2 预设菜单
INSERT INTO menu (name, path, component, icon, sort, parent_id, type, permission, status, created_at) VALUES
-- 仪表盘
('仪表盘', '/dashboard', 'dashboard/index', 'HomeIcon', 1, 0, 1, 'dashboard:view', 1, NOW()),
-- 商品管理
('商品管理', '/product', 'product/index', 'CubeIcon', 10, 0, 1, 'spu:view', 1, NOW()),
('商品列表', '/product/list', 'product/List', '', 1, 10, 1, 'spu:list', 1, NOW()),
('新增商品', '', '', '', 2, 10, 2, 'spu:add', 1, NOW()),
('编辑商品', '', '', '', 3, 10, 2, 'spu:edit', 1, NOW()),
('删除商品', '', '', '', 4, 10, 2, 'spu:delete', 1, NOW()),
('商品导入', '', '', '', 5, 10, 2, 'spu:import', 1, NOW()),
('商品导出', '', '', '', 6, 10, 2, 'spu:export', 1, NOW()),
('规格管理', '/product/spec', 'product/spec', '', 7, 10, 1, 'spec:view', 1, NOW()),
('分类管理', '/product/category', 'product/category', '', 8, 10, 1, 'category:view', 1, NOW()),
-- SN码管理
('SN码管理', '/sn', 'sn/index', 'QrCodeIcon', 20, 0, 1, 'sn:view', 1, NOW()),
('SN码录入', '', '', '', 1, 20, 2, 'sn:add', 1, NOW()),
('SN码批量导入', '', '', '', 2, 20, 2, 'sn:import', 1, NOW()),
('SN码查询', '', '', '', 3, 20, 2, 'sn:query', 1, NOW()),
-- 客户管理
('客户管理', '/customer', 'customer/index', 'UsersIcon', 30, 0, 1, 'customer:view', 1, NOW()),
('客户列表', '/customer/list', 'customer/List', '', 1, 30, 1, 'customer:list', 1, NOW()),
-- 订单管理
('订单管理', '/order', 'order/index', 'ShoppingCartIcon', 40, 0, 1, 'order:view', 1, NOW()),
('订单列表', '/order/list', 'order/List', '', 1, 40, 1, 'order:list', 1, NOW()),
-- 统计报表
('统计报表', '/statistics', 'statistics/index', 'ChartBarIcon', 45, 0, 1, 'statistics:view', 1, NOW()),
-- 系统管理
('系统管理', '/system', 'system/index', 'CogIcon', 50, 0, 1, 'system:view', 1, NOW()),
('员工管理', '/system/user', 'system/user', '', 1, 50, 1, 'system:user', 1, NOW()),
('角色管理', '/system/role', 'system/role', '', 2, 50, 1, 'system:role', 1, NOW()),
('菜单管理', '/system/menu', 'system/menu', '', 3, 50, 1, 'system:menu', 1, NOW()),
('操作日志', '/system/log', 'system/log', '', 4, 50, 1, 'system:log', 1, NOW());

-- 6.3 会员等级配置
INSERT INTO member_level_config (level, name, consume_threshold, discount, points_rate, status, created_at) VALUES
(1, '普通', 0, 1.00, 1, 1, NOW()),
(2, '银卡', 1000, 0.95, 1.5, 1, NOW()),
(3, '金卡', 5000, 0.90, 2, 1, NOW()),
(4, '钻石', 20000, 0.85, 3, 1, NOW());

-- 6.4 规格配置
INSERT INTO spec_name (name, sort, status) VALUES
('颜色', 1, 1),
('尺寸', 2, 1),
('内存', 3, 1);

INSERT INTO spec_value (spec_id, value, sort, status) VALUES
(1, '黑色', 1, 1), (1, '白色', 2, 1), (1, '蓝色', 3, 1),
(2, 'S', 1, 1), (2, 'M', 2, 1), (2, 'L', 3, 1), (2, 'XL', 4, 1),
(3, '128G', 1, 1), (3, '256G', 2, 1), (3, '512G', 3, 1);

-- 6.5 超级管理员
-- 密码: 123456 (BCrypt加密)
INSERT INTO admin_user (username, password, real_name, role_id, status, created_at, updated_at) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '超级管理员', 1, 1, NOW(), NOW());

-- 6.6 预设商品分类
INSERT INTO goods_category (name, parent_id, sort, status, created_at, updated_at) VALUES
('电子产品', 0, 1, 1, NOW(), NOW()),
('手机', 1, 1, 1, NOW(), NOW()),
('电脑', 1, 2, 1, NOW(), NOW()),
('配件', 1, 3, 1, NOW(), NOW()),
('服装', 0, 2, 1, NOW(), NOW()),
('男装', 5, 1, 1, NOW(), NOW()),
('女装', 5, 2, 1, NOW(), NOW());

-- =====================================================
-- 完成
-- =====================================================