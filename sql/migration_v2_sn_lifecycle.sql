-- =====================================================
-- 销售管理系统 - 数据库迁移脚本 v2.0
-- 说明: SN码全生命周期 + 订单物流字段 + 退款流程
-- 作者: SOLO
-- 日期: 2026-07-02
-- 适用: 已存在 init.sql 的数据库增量更新
-- =====================================================

-- 使用数据库
USE sale_manager;

-- =====================================================
-- 第一部分: 订单表新增物流字段 (核心 - 修复 500 错误)
-- 当前 order 表已有: cancel_time, finish_time
-- 需要新增: logistics_no, logistics_company, shipped_at, received_at
-- =====================================================

-- 1.1 添加物流字段(在 remark 字段之后)
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = 'sale_manager'
       AND TABLE_NAME = 'order'
       AND COLUMN_NAME = 'logistics_no') = 0,
    'ALTER TABLE `order` ADD COLUMN logistics_no VARCHAR(64) COMMENT ''物流单号'' AFTER remark',
    'SELECT ''logistics_no 已存在'' AS info'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = 'sale_manager'
       AND TABLE_NAME = 'order'
       AND COLUMN_NAME = 'logistics_company') = 0,
    'ALTER TABLE `order` ADD COLUMN logistics_company VARCHAR(64) COMMENT ''物流公司'' AFTER logistics_no',
    'SELECT ''logistics_company 已存在'' AS info'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = 'sale_manager'
       AND TABLE_NAME = 'order'
       AND COLUMN_NAME = 'shipped_at') = 0,
    'ALTER TABLE `order` ADD COLUMN shipped_at DATETIME COMMENT ''发货时间'' AFTER logistics_company',
    'SELECT ''shipped_at 已存在'' AS info'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = 'sale_manager'
       AND TABLE_NAME = 'order'
       AND COLUMN_NAME = 'received_at') = 0,
    'ALTER TABLE `order` ADD COLUMN received_at DATETIME COMMENT ''签收时间'' AFTER shipped_at',
    'SELECT ''received_at 已存在'' AS info'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 1.2 添加物流单号索引(如果不存在)
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = 'sale_manager'
       AND TABLE_NAME = 'order'
       AND INDEX_NAME = 'idx_logistics') = 0,
    'ALTER TABLE `order` ADD INDEX idx_logistics (logistics_no)',
    'SELECT ''idx_logistics 已存在'' AS info'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 1.3 扩展订单状态字段注释 (0-7 共8种状态)
ALTER TABLE `order`
    MODIFY COLUMN status TINYINT DEFAULT 0
    COMMENT '订单状态: 0待支付 1已支付 2已发货 3已签收 4已完成 5已取消 6退款中 7已退款';

-- =====================================================
-- 第二部分: SN码状态扩展 (0-8 共9种状态)
-- 0在库 1锁定 2已售 3已发货 4已签收 5已完成 6已作废 7退货中 8已退货
-- TINYINT 范围足够,仅扩展注释
-- =====================================================

-- 2.1 扩展 sn_code.status 注释
ALTER TABLE sn_code
    MODIFY COLUMN status TINYINT DEFAULT 0
    COMMENT '状态: 0在库 1锁定 2已售 3已发货 4已签收 5已完成 6已作废 7退货中 8已退货';

-- 2.1.1 sn_code 增加扫码流转字段 (扫码入库/发货/签收/退货等)
-- 注意: 先判断字段是否存在再添加 (幂等迁移)
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = 'sale_manager'
       AND TABLE_NAME = 'sn_code'
       AND COLUMN_NAME = 'batch_no') = 0,
    'ALTER TABLE sn_code
        ADD COLUMN batch_no VARCHAR(64) COMMENT ''批次号'' AFTER source,
        ADD COLUMN inbound_at DATETIME COMMENT ''扫码入库时间'' AFTER batch_no,
        ADD COLUMN inbound_user_id BIGINT COMMENT ''入库操作人ID'' AFTER inbound_at,
        ADD COLUMN inbound_user_name VARCHAR(64) COMMENT ''入库操作人姓名'' AFTER inbound_user_id,
        ADD COLUMN delivered_at DATETIME COMMENT ''发货时间'' AFTER inbound_user_name,
        ADD COLUMN received_at DATETIME COMMENT ''签收时间'' AFTER delivered_at,
        ADD COLUMN return_at DATETIME COMMENT ''退货时间'' AFTER received_at,
        ADD COLUMN current_holder VARCHAR(128) COMMENT ''当前持有者'' AFTER return_at,
        ADD COLUMN current_location VARCHAR(128) COMMENT ''当前位置'' AFTER current_holder,
        ADD COLUMN logistics_no VARCHAR(64) COMMENT ''物流单号'' AFTER current_location,
        ADD COLUMN remark VARCHAR(256) COMMENT ''备注'' AFTER logistics_no,
        ADD INDEX idx_batch_no (batch_no),
        ADD INDEX idx_inbound (inbound_at)',
    'SELECT ''sn_code 扫码流转字段已存在'' AS info'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 2.2 扩展 sn_code_log 状态注释
ALTER TABLE sn_code_log
    MODIFY COLUMN from_status TINYINT
    COMMENT '操作前状态: 0在库 1锁定 2已售 3已发货 4已签收 5已完成 6已作废 7退货中 8已退货';

ALTER TABLE sn_code_log
    MODIFY COLUMN to_status TINYINT
    COMMENT '操作后状态: 0在库 1锁定 2已售 3已发货 4已签收 5已完成 6已作废 7退货中 8已退货';

-- 2.3 sn_code_log 增加关联字段(订单ID、退款单ID) - 仅在不存在时
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = 'sale_manager'
       AND TABLE_NAME = 'sn_code_log'
       AND COLUMN_NAME = 'order_id') = 0,
    'ALTER TABLE sn_code_log ADD COLUMN order_id BIGINT COMMENT ''关联订单ID'' AFTER operator_name,
                          ADD COLUMN refund_apply_id BIGINT COMMENT ''关联退款单ID'' AFTER order_id,
                          ADD INDEX idx_order (order_id),
                          ADD INDEX idx_refund_apply (refund_apply_id)',
    'SELECT ''sn_code_log.order_id 已存在'' AS info'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =====================================================
-- 第三部分: 订单SN码绑定表 (创建 + 字段补全)
-- =====================================================

CREATE TABLE IF NOT EXISTS order_sn_bind (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_item_id BIGINT NOT NULL COMMENT '订单明细ID',
    sn_code_id BIGINT NOT NULL COMMENT 'SN码ID',
    sn_code VARCHAR(64) NOT NULL COMMENT 'SN码值',
    operation VARCHAR(32) COMMENT '操作: create-下单锁定 pay-支付 sold-已售 ship-发货 receive-签收 complete-完成 refund_apply-退货申请 refund_in-退货入库',
    operator_id BIGINT COMMENT '操作人ID',
    operator_name VARCHAR(64) COMMENT '操作人姓名',
    created_at DATETIME,
    UNIQUE KEY uk_order_sn (order_id, sn_code_id),
    INDEX idx_sn (sn_code_id),
    INDEX idx_order_item (order_item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单SN码绑定表';

-- 补全字段(表可能已存在)
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = 'sale_manager'
       AND TABLE_NAME = 'order_sn_bind'
       AND COLUMN_NAME = 'operator_id') = 0,
    'ALTER TABLE order_sn_bind ADD COLUMN operator_id BIGINT COMMENT ''操作人ID'' AFTER operation,
                            ADD COLUMN operator_name VARCHAR(64) COMMENT ''操作人姓名'' AFTER operator_id',
    'SELECT ''order_sn_bind.operator_id 已存在'' AS info'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =====================================================
-- 第四部分: 退款申请表 (创建 + 字段扩展)
-- =====================================================

CREATE TABLE IF NOT EXISTS refund_apply (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    order_no VARCHAR(64) COMMENT '订单号',
    customer_id BIGINT COMMENT '客户ID',
    customer_name VARCHAR(50) COMMENT '客户名称',
    refund_amount DECIMAL(12,2) COMMENT '退款金额',
    reason VARCHAR(500) COMMENT '退款原因',
    express_company VARCHAR(64) COMMENT '快递公司',
    express_no VARCHAR(64) COMMENT '快递单号',
    images TEXT COMMENT '图片凭证JSON',
    status INT DEFAULT 0 COMMENT '状态：0待审核 1已通过 2已拒绝 3退货中 4已入库 5已退款',
    audit_remark VARCHAR(500) COMMENT '审核备注',
    audit_user_id BIGINT COMMENT '审核人ID',
    audit_at DATETIME COMMENT '审核时间',
    warehouse_in_at DATETIME COMMENT '入库时间',
    warehouse_in_user_id BIGINT COMMENT '入库操作人ID',
    warehouse_in_user_name VARCHAR(64) COMMENT '入库操作人姓名',
    refund_at DATETIME COMMENT '退款完成时间',
    created_at DATETIME,
    updated_at DATETIME,
    INDEX idx_order (order_id),
    INDEX idx_status (status),
    INDEX idx_express (express_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款申请表（SN码退货）';

-- 4.1 补全字段(表可能已存在)
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = 'sale_manager'
       AND TABLE_NAME = 'refund_apply'
       AND COLUMN_NAME = 'express_company') = 0,
    'ALTER TABLE refund_apply ADD COLUMN express_company VARCHAR(64) COMMENT ''快递公司'' AFTER reason,
                            ADD COLUMN express_no VARCHAR(64) COMMENT ''快递单号'' AFTER express_company,
                            ADD COLUMN warehouse_in_user_id BIGINT COMMENT ''入库操作人ID'' AFTER warehouse_in_at,
                            ADD COLUMN warehouse_in_user_name VARCHAR(64) COMMENT ''入库操作人姓名'' AFTER warehouse_in_user_id',
    'SELECT ''refund_apply.express 字段已存在'' AS info'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 4.2 添加快递单号索引(如果不存在)
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
     WHERE TABLE_SCHEMA = 'sale_manager'
       AND TABLE_NAME = 'refund_apply'
       AND INDEX_NAME = 'idx_express') = 0,
    'ALTER TABLE refund_apply ADD INDEX idx_express (express_no)',
    'SELECT ''idx_express 已存在'' AS info'
));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- =====================================================
-- 第五部分: 新建表 - 退款SN码扫码入库明细表
-- =====================================================

CREATE TABLE IF NOT EXISTS refund_apply_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    refund_apply_id BIGINT NOT NULL COMMENT '退款申请ID',
    sn_code_id BIGINT NOT NULL COMMENT 'SN码ID',
    sn_code VARCHAR(64) NOT NULL COMMENT 'SN码值',
    sku_id BIGINT COMMENT 'SKU ID',
    spu_name VARCHAR(128) COMMENT '商品名称',
    sku_spec VARCHAR(255) COMMENT '规格',
    scanned_at DATETIME COMMENT '扫码时间',
    scan_user_id BIGINT COMMENT '扫码操作人ID',
    scan_user_name VARCHAR(64) COMMENT '扫码操作人姓名',
    created_at DATETIME COMMENT '创建时间',
    UNIQUE KEY uk_refund_sn (refund_apply_id, sn_code_id),
    INDEX idx_sn (sn_code_id),
    INDEX idx_refund (refund_apply_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款SN码扫码入库明细表';

-- =====================================================
-- 第六部分: 余额变动记录类型扩展
-- =====================================================

ALTER TABLE balance_record
    MODIFY COLUMN type TINYINT NOT NULL
    COMMENT '类型: 1充值 2消费 3退款 4调整 5退货退款';

-- =====================================================
-- 第七部分: 创建 SN码状态统计视图
-- =====================================================

DROP VIEW IF EXISTS v_sn_code_stats;
CREATE VIEW v_sn_code_stats AS
SELECT
    spu_id,
    spu_name,
    sku_id,
    sku_code,
    status,
    CASE status
        WHEN 0 THEN '在库'
        WHEN 1 THEN '锁定'
        WHEN 2 THEN '已售'
        WHEN 3 THEN '已发货'
        WHEN 4 THEN '已签收'
        WHEN 5 THEN '已完成'
        WHEN 6 THEN '已作废'
        WHEN 7 THEN '退货中'
        WHEN 8 THEN '已退货'
        ELSE '未知'
    END AS status_name,
    COUNT(*) AS total
FROM sn_code
GROUP BY spu_id, spu_name, sku_id, sku_code, status;

-- =====================================================
-- 第八部分: 数据迁移 (兼容旧状态码)
-- =====================================================

-- 旧 SN 状态码: 0在库 1已售 2已作废 3退货中 4已退货
-- 新 SN 状态码: 0在库 1锁定 2已售 3已发货 4已签收 5已完成 6已作废 7退货中 8已退货
-- 旧 1-已售 → 新 5-已完成 (因为是已完成的销售)
UPDATE sn_code SET status = 5 WHERE status = 1;
-- 旧 2-已作废 → 新 6-已作废
UPDATE sn_code SET status = 6 WHERE status = 2;
-- 旧 3-退货中 → 新 7-退货中 (一致)
UPDATE sn_code SET status = 7 WHERE status = 3;
-- 旧 4-已退货 → 新 8-已退货 (一致)
UPDATE sn_code SET status = 8 WHERE status = 4;
-- 旧 0-在库 → 仍为 0-在库

-- 旧订单状态码: 0待付款 1已付款 2已完成 3已取消 4退款中 5已退款
-- 新订单状态码: 0待支付 1已支付 2已发货 3已签收 4已完成 5已取消 6退款中 7已退款
-- 旧 0-待付款 → 新 0-待支付 (一致)
-- 旧 1-已付款 → 新 1-已支付 (一致)
-- 旧 2-已完成 → 新 4-已完成 (差2)
UPDATE `order` SET status = 4 WHERE status = 2;
-- 旧 3-已取消 → 新 5-已取消 (差2)
UPDATE `order` SET status = 5 WHERE status = 3;
-- 旧 4-退款中 → 新 6-退款中 (差2)
UPDATE `order` SET status = 6 WHERE status = 4;
-- 旧 5-已退款 → 新 7-已退款 (差2)
UPDATE `order` SET status = 7 WHERE status = 5;

-- =====================================================
-- 脚本结束
-- =====================================================
SELECT '✅ Migration v2.0 SN码全生命周期 执行完成' AS result;