-- 给 `order` 表加 4 个时间字段
-- （SaleOrder 实体新增了 shippedAt / receivedAt / refundAt / refundCompleteAt 4 个字段，对应数据库列：ship_time / receive_time / refund_time / refund_complete_time）

ALTER TABLE `order`
    ADD COLUMN `ship_time` DATETIME DEFAULT NULL COMMENT '发货时间' AFTER `pay_time`,
    ADD COLUMN `receive_time` DATETIME DEFAULT NULL COMMENT '签收时间' AFTER `ship_time`,
    ADD COLUMN `refund_time` DATETIME DEFAULT NULL COMMENT '申请退款时间' AFTER `receive_time`,
    ADD COLUMN `refund_complete_time` DATETIME DEFAULT NULL COMMENT '退款完成时间' AFTER `refund_time`;

-- 验证
DESC `order`;
