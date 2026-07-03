package com.salemanager.modules.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.customer.mapper.AddressMapper;
import com.salemanager.modules.customer.mapper.CustomerMapper;
import com.salemanager.modules.customer.model.Address;
import com.salemanager.modules.customer.model.Customer;
import com.salemanager.modules.customer.service.AppOrderService;
import com.salemanager.modules.product.mapper.SkuMapper;
import com.salemanager.modules.product.model.Sku;
import com.salemanager.modules.sale.mapper.SaleOrderItemMapper;
import com.salemanager.modules.sale.mapper.SaleOrderMapper;
import com.salemanager.modules.sale.model.SaleOrder;
import com.salemanager.modules.sale.model.SaleOrderItem;
import com.salemanager.modules.sn.mapper.SnCodeMapper;
import com.salemanager.modules.sn.model.SnCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 小程序端订单服务。
 *
 * 业务流程（演示版）：
 *
 *   下单（createOrder）
 *      ├─ 校验地址与 SKU
 *      ├─ 每个明细：从 sn_code 取 N 条在库 SN 码（status=0）
 *      ├─ 写入 order(0待支付) + order_item
 *      ├─ sn_code.status 0 → 1（锁定）
 *      └─ 写 sn_code_log
 *
 *   支付（payOrder）
 *      └─ sn_code.status 1 → 2（已售），order.status 0 → 1（已支付）
 *      └─ 演示版不做真实发 / 收货
 *
 *   取消（cancelOrder）
 *      └─ sn_code.status 1 → 0（回滚在库），order.status 0 → 5（已取消）
 *
 *   收货（deliverThenReceive）
 *      └─ 演示版简化：order.status 1 → 3（已签收）
 */
@Service
public class AppOrderServiceImpl implements AppOrderService {

    private static final Logger log = LoggerFactory.getLogger(AppOrderServiceImpl.class);

    @Autowired private CustomerMapper customerMapper;
    @Autowired private AddressMapper addressMapper;
    @Autowired private SkuMapper skuMapper;
    @Autowired private SnCodeMapper snCodeMapper;
    @Autowired private SaleOrderMapper saleOrderMapper;
    @Autowired private SaleOrderItemMapper saleOrderItemMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public Long createOrder(Long customerId, CreateOrderParam param) {
        if (customerId == null) throw new BusinessException("未登录");
        if (param == null || param.items == null || param.items.isEmpty()) {
            throw new BusinessException("订单明细不能为空");
        }
        Address address = param.addressId == null ? null : addressMapper.selectById(param.addressId);
        if (address == null) {
            throw new BusinessException("收货地址无效");
        }
        if (!customerId.equals(address.getCustomerId())) {
            throw new BusinessException("收货地址不属于当前账户");
        }
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) throw new BusinessException("账户不存在");

        SaleOrder order = new SaleOrder();
        order.setOrderNo(generateOrderNo());
        order.setCustomerId(customerId);
        order.setCustomerName(customer.getNickname());
        order.setAddressId(address.getId());
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverAddress(joinAddress(address));
        order.setStatus(0); // 待支付
        order.setPayType(1); // 默认微信
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<SaleOrderItem> items = new ArrayList<>();
        // 收集每条明细的锁定 SN ID 列表
        Map<Long, List<Long>> itemSnMap = new HashMap<>();

        // 第一阶段：检查并准备
        for (AppOrderService.Item it : param.items) {
            if (it.skuId == null || it.quantity == null || it.quantity <= 0) {
                throw new BusinessException("订单明细参数错误");
            }
            Sku sku = skuMapper.selectById(it.skuId);
            if (sku == null) throw new BusinessException("SKU 不存在: " + it.skuId);

            // 取出在库 SN 码
            List<SnCode> snList = snCodeMapper.selectList(
                    new LambdaQueryWrapper<SnCode>()
                            .eq(SnCode::getSkuId, it.skuId)
                            .eq(SnCode::getStatus, 0)
                            .last("LIMIT " + it.quantity)
                            .orderByAsc(SnCode::getId));
            if (snList.size() < it.quantity) {
                throw new BusinessException("商品库存不足，最多可购买 " + snList.size() + " 件");
            }

            SaleOrderItem oi = new SaleOrderItem();
            oi.setSkuId(it.skuId);
            oi.setSpuName(sku.getSkuCode());
            // 预留 position：插入后会回填 ID
            items.add(oi);

            BigDecimal line = sku.getPrice() == null ? BigDecimal.ZERO : sku.getPrice();
            oi.setPrice(line);
            oi.setQuantity(it.quantity);
            oi.setSubtotal(line.multiply(BigDecimal.valueOf(it.quantity)));

            totalAmount = totalAmount.add(oi.getSubtotal());

            // SN 暂存（先用 skuId 作 key，再映射到明细 ID）
            List<Long> snIds = new ArrayList<>();
            for (SnCode sn : snList) snIds.add(sn.getId());
            itemSnMap.put(it.skuId, snIds);
        }

        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(totalAmount);
        saleOrderMapper.insert(order);

        // 第二阶段：写入明细 + 锁定 SN
        for (SaleOrderItem oi : items) {
            oi.setOrderId(order.getId());
            oi.setOrderNo(order.getOrderNo());
            oi.setCreatedAt(LocalDateTime.now());
            saleOrderItemMapper.insert(oi);

            List<Long> snIds = itemSnMap.get(oi.getSkuId());
            try {
                oi.setSnCodeIds(objectMapper.writeValueAsString(snIds));
                saleOrderItemMapper.updateById(oi);
            } catch (JsonProcessingException e) {
                throw new BusinessException("订单明细序列化失败");
            }

            if (snIds != null && !snIds.isEmpty()) {
                for (Long snId : snIds) {
                    SnCode sn = snCodeMapper.selectById(snId);
                    if (sn == null) continue;
                    Integer before = sn.getStatus();
                    snCodeMapper.update(null, new LambdaUpdateWrapper<SnCode>()
                            .eq(SnCode::getId, snId)
                            .set(SnCode::getStatus, 1)       // 锁定
                            .set(SnCode::getUpdatedAt, LocalDateTime.now()));
                    log.info("订单锁定 SN id={}, sn={}, orderId={}, status={}->1", snId, sn.getSnCode(), order.getId(), before);
                }
            }
        }

        log.info("订单创建成功 customerId={}, orderId={}, orderNo={}, amount={}",
                customerId, order.getId(), order.getOrderNo(), totalAmount);
        return order.getId();
    }

    @Override
    public List<Map<String, Object>> listByCustomer(Long customerId, Integer status, Integer page, Integer pageSize) {
        if (customerId == null) throw new BusinessException("未登录");
        if (page == null || page < 1) page = 1;
        if (pageSize == null || pageSize < 1) pageSize = 20;

        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<SaleOrder>()
                .eq(SaleOrder::getCustomerId, customerId)
                .orderByDesc(SaleOrder::getCreatedAt);
        if (status != null) wrapper.eq(SaleOrder::getStatus, status);

        Page<SaleOrder> p = saleOrderMapper.selectPage(new Page<>(page, pageSize), wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (SaleOrder o : p.getRecords()) {
            Map<String, Object> vo = toOrderVO(o, true);
            result.add(vo);
        }
        return result;
    }

    @Override
    public Map<String, Object> getDetail(Long customerId, Long orderId) {
        SaleOrder o = mustOwned(customerId, orderId);
        return toOrderVO(o, true);
    }

    @Override
    @Transactional
    public void payOrder(Long customerId, Long orderId, Integer payType) {
        SaleOrder order = mustOwned(customerId, orderId);
        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不允许支付: " + order.getStatus());
        }
        SaleOrderItem item = saleOrderItemMapper.selectOne(
                new LambdaQueryWrapper<SaleOrderItem>().eq(SaleOrderItem::getOrderId, order.getId()));
        List<Long> snIds = parseSnIds(item == null ? null : item.getSnCodeIds());
        if (snIds != null) {
            for (Long snId : snIds) {
                SnCode sn = snCodeMapper.selectById(snId);
                if (sn == null) continue;
                Integer before = sn.getStatus();
                snCodeMapper.update(null, new LambdaUpdateWrapper<SnCode>()
                        .eq(SnCode::getId, snId)
                        .set(SnCode::getStatus, 2)              // 已售
                        .set(SnCode::getSoldAt, LocalDateTime.now())
                        .set(SnCode::getUpdatedAt, LocalDateTime.now()));
                log.info("订单支付 SN 已售 id={}, sn={}, status={}->2", snId, sn.getSnCode(), before);
            }
        }
        saleOrderMapper.update(null, new LambdaUpdateWrapper<SaleOrder>()
                .eq(SaleOrder::getId, orderId)
                .set(SaleOrder::getStatus, 1)                       // 已支付
                .set(SaleOrder::getPayType, payType == null ? 1 : payType)
                .set(SaleOrder::getPaidAt, LocalDateTime.now())
                .set(SaleOrder::getUpdatedAt, LocalDateTime.now()));
        log.info("订单支付成功 orderId={}, customerId={}", orderId, customerId);
    }

    @Override
    @Transactional
    public void cancelOrder(Long customerId, Long orderId) {
        SaleOrder order = mustOwned(customerId, orderId);
        if (order.getStatus() != 0) {
            throw new BusinessException("当前订单状态无法取消");
        }
        SaleOrderItem item = saleOrderItemMapper.selectOne(
                new LambdaQueryWrapper<SaleOrderItem>().eq(SaleOrderItem::getOrderId, order.getId()));
        List<Long> snIds = parseSnIds(item == null ? null : item.getSnCodeIds());
        if (snIds != null) {
            for (Long snId : snIds) {
                SnCode sn = snCodeMapper.selectById(snId);
                if (sn == null) continue;
                Integer before = sn.getStatus();
                snCodeMapper.update(null, new LambdaUpdateWrapper<SnCode>()
                        .eq(SnCode::getId, snId)
                        .set(SnCode::getStatus, 0)
                        .set(SnCode::getUpdatedAt, LocalDateTime.now()));
                log.info("订单取消 SN 回滚 id={}, sn={}, status={}->0", snId, sn.getSnCode(), before);
            }
        }
        saleOrderMapper.update(null, new LambdaUpdateWrapper<SaleOrder>()
                .eq(SaleOrder::getId, orderId)
                .set(SaleOrder::getStatus, 5)                       // 已取消
                .set(SaleOrder::getCancelledAt, LocalDateTime.now())
                .set(SaleOrder::getUpdatedAt, LocalDateTime.now()));
        log.info("订单取消 orderId={}, customerId={}", orderId, customerId);
    }

    @Override
    @Transactional
    public void deliverThenReceive(Long customerId, Long orderId) {
        SaleOrder order = mustOwned(customerId, orderId);
        if (order.getStatus() != 2) {
            throw new BusinessException("当前订单状态无法签收");
        }
        SaleOrderItem item = saleOrderItemMapper.selectOne(
                new LambdaQueryWrapper<SaleOrderItem>().eq(SaleOrderItem::getOrderId, order.getId()));
        List<Long> snIds = parseSnIds(item == null ? null : item.getSnCodeIds());
        if (snIds != null) {
            for (Long snId : snIds) {
                SnCode sn = snCodeMapper.selectById(snId);
                if (sn == null) continue;
                Integer before = sn.getStatus();
                snCodeMapper.update(null, new LambdaUpdateWrapper<SnCode>()
                        .eq(SnCode::getId, snId)
                        .set(SnCode::getStatus, 4)                  // 已签收
                        .set(SnCode::getReceivedAt, LocalDateTime.now())
                        .set(SnCode::getUpdatedAt, LocalDateTime.now()));
                log.info("订单签收 SN id={}, sn={}, status={}->4", snId, sn.getSnCode(), before);
            }
        }
        saleOrderMapper.update(null, new LambdaUpdateWrapper<SaleOrder>()
                .eq(SaleOrder::getId, orderId)
                .set(SaleOrder::getStatus, 3)                       // 已签收
                .set(SaleOrder::getUpdatedAt, LocalDateTime.now()));
        log.info("订单签收 orderId={}, customerId={}", orderId, customerId);
    }

    @Override
    @Transactional
    public void shipOrder(Long customerId, Long orderId) {
        SaleOrder order = mustOwned(customerId, orderId);
        if (order.getStatus() != 1) {
            throw new BusinessException("只有已支付的订单才能发货");
        }
        SaleOrderItem item = saleOrderItemMapper.selectOne(
                new LambdaQueryWrapper<SaleOrderItem>().eq(SaleOrderItem::getOrderId, order.getId()));
        List<Long> snIds = parseSnIds(item == null ? null : item.getSnCodeIds());
        if (snIds != null) {
            for (Long snId : snIds) {
                SnCode sn = snCodeMapper.selectById(snId);
                if (sn == null) continue;
                Integer before = sn.getStatus();
                snCodeMapper.update(null, new LambdaUpdateWrapper<SnCode>()
                        .eq(SnCode::getId, snId)
                        .set(SnCode::getStatus, 3)                  // 已发货
                        .set(SnCode::getDeliveredAt, LocalDateTime.now())
                        .set(SnCode::getUpdatedAt, LocalDateTime.now()));
                log.info("订单发货 SN id={}, sn={}, status={}->3", snId, sn.getSnCode(), before);
            }
        }
        saleOrderMapper.update(null, new LambdaUpdateWrapper<SaleOrder>()
                .eq(SaleOrder::getId, orderId)
                .set(SaleOrder::getStatus, 2)                       // 已发货
                .set(SaleOrder::getShippedAt, LocalDateTime.now())
                .set(SaleOrder::getUpdatedAt, LocalDateTime.now()));
        log.info("订单发货 orderId={}, customerId={}", orderId, customerId);
    }

    @Override
    @Transactional
    public void manualCompleteRefund(Long customerId, Long orderId) {
        SaleOrder order = mustOwned(customerId, orderId);
        if (order.getStatus() != 6) {
            throw new BusinessException("只有退款中的订单才能完成退款");
        }
        SaleOrderItem item = saleOrderItemMapper.selectOne(
                new LambdaQueryWrapper<SaleOrderItem>().eq(SaleOrderItem::getOrderId, order.getId()));
        List<Long> snIds = parseSnIds(item == null ? null : item.getSnCodeIds());
        if (snIds != null) {
            for (Long snId : snIds) {
                SnCode sn = snCodeMapper.selectById(snId);
                if (sn == null) continue;
                Integer before = sn.getStatus();
                snCodeMapper.update(null, new LambdaUpdateWrapper<SnCode>()
                        .eq(SnCode::getId, snId)
                        .set(SnCode::getStatus, 8)                  // 已退货
                        .set(SnCode::getUpdatedAt, LocalDateTime.now()));
                log.info("退款完成 SN 退货 id={}, sn={}, status={}->8", snId, sn.getSnCode(), before);
            }
        }
        saleOrderMapper.update(null, new LambdaUpdateWrapper<SaleOrder>()
                .eq(SaleOrder::getId, orderId)
                .set(SaleOrder::getStatus, 7)                       // 已退款
                .set(SaleOrder::getRefundCompleteAt, LocalDateTime.now())
                .set(SaleOrder::getUpdatedAt, LocalDateTime.now()));
        log.info("退款完成 orderId={}, customerId={}", orderId, customerId);
    }

    @Override
    @Transactional
    public Long applyRefund(Long customerId, Long orderId, String reason) {
        SaleOrder order = mustOwned(customerId, orderId);
        if (order.getStatus() != 1 && order.getStatus() != 2 && order.getStatus() != 3) {
            throw new BusinessException("当前订单状态无法退款");
        }
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException("请填写退款原因");
        }
        SaleOrderItem item = saleOrderItemMapper.selectOne(
                new LambdaQueryWrapper<SaleOrderItem>().eq(SaleOrderItem::getOrderId, order.getId()));
        List<Long> snIds = parseSnIds(item == null ? null : item.getSnCodeIds());
        if (snIds != null) {
            for (Long snId : snIds) {
                SnCode sn = snCodeMapper.selectById(snId);
                if (sn == null) continue;
                Integer before = sn.getStatus();
                snCodeMapper.update(null, new LambdaUpdateWrapper<SnCode>()
                        .eq(SnCode::getId, snId)
                        .set(SnCode::getStatus, 7)                  // 退货中
                        .set(SnCode::getReturnAt, LocalDateTime.now())
                        .set(SnCode::getUpdatedAt, LocalDateTime.now()));
                log.info("申请退款 SN 退货中 id={}, sn={}, status={}->7", snId, sn.getSnCode(), before);
            }
        }
        saleOrderMapper.update(null, new LambdaUpdateWrapper<SaleOrder>()
                .eq(SaleOrder::getId, orderId)
                .set(SaleOrder::getStatus, 6)                       // 退款中
                .set(SaleOrder::getUpdatedAt, LocalDateTime.now()));
        log.info("申请退款 orderId={}, customerId={}, reason={}", orderId, customerId, reason);
        return order.getId();
    }

    // ============================== 私有辅助 ==============================

    private SaleOrder mustOwned(Long customerId, Long orderId) {
        SaleOrder o = saleOrderMapper.selectById(orderId);
        if (o == null) throw new BusinessException("订单不存在");
        if (!customerId.equals(o.getCustomerId())) {
            throw new BusinessException("无权访问该订单");
        }
        return o;
    }

    private Map<String, Object> toOrderVO(SaleOrder o, boolean withItems) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", o.getId());
        vo.put("orderNo", o.getOrderNo());
        vo.put("customerId", o.getCustomerId());
        vo.put("customerName", o.getCustomerName());
        vo.put("receiverName", o.getReceiverName());
        vo.put("receiverPhone", o.getReceiverPhone());
        vo.put("address", o.getReceiverAddress());
        vo.put("totalAmount", o.getTotalAmount());
        vo.put("payAmount", o.getPayAmount());
        vo.put("payType", o.getPayType());
        vo.put("status", o.getStatus());
        vo.put("remark", o.getRemark());
        vo.put("createdAt", o.getCreatedAt());
        vo.put("payTime", o.getPaidAt());
        vo.put("shipTime", o.getShippedAt());
        vo.put("receiveTime", o.getReceivedAt());
        vo.put("refundTime", o.getRefundAt());
        vo.put("refundCompleteTime", o.getRefundCompleteAt());
        vo.put("cancelTime", o.getCancelledAt());

        if (withItems) {
            List<Map<String, Object>> items = new ArrayList<>();
            List<SaleOrderItem> orderItems = saleOrderItemMapper.selectList(
                    new LambdaQueryWrapper<SaleOrderItem>().eq(SaleOrderItem::getOrderId, o.getId()));
            for (SaleOrderItem it : orderItems) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", it.getId());
                m.put("skuId", it.getSkuId());
                m.put("spuId", resolveSpuId(it.getSkuId()));
                m.put("goodsName", it.getSpuName()); // 冗余返回，下游可读 goodsName
                m.put("spuName", it.getSpuName());
                m.put("spec", it.getSkuSpec());
                m.put("imageUrl", it.getSkuImage());
                m.put("price", it.getPrice());
                m.put("quantity", it.getQuantity());
                m.put("subtotal", it.getSubtotal());
                m.put("snCodes", it.getSnCodeIds());
                // 解析 SN 码列表（id, code, status, statusName, updatedAt）
                m.put("snList", resolveSnList(it.getSnCodeIds()));
                items.add(m);
            }
            vo.put("items", items);
        }
        return vo;
    }

    private Long resolveSpuId(Long skuId) {
        if (skuId == null) return null;
        Sku s = skuMapper.selectById(skuId);
        return s == null ? null : s.getSpuId();
    }

    /** SN 状态码 → 中文名 */
    private String snStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "在库";
            case 1: return "已锁定";
            case 2: return "已售";
            case 3: return "已发货";
            case 4: return "已签收";
            case 5: return "已取消";
            case 6: return "已退款";
            case 7: return "退货中";
            case 8: return "已退货";
            default: return "未知(" + status + ")";
        }
    }

    /**
     * 解析订单明细中的 snCodeIds 字段，查询并返回每条 SN 的详情
     */
    private List<Map<String, Object>> resolveSnList(String snCodeIdsJson) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (snCodeIdsJson == null || snCodeIdsJson.isEmpty()) return out;
        List<Long> snIds;
        try {
            snIds = objectMapper.readValue(snCodeIdsJson, List.class);
        } catch (Exception e) {
            return out;
        }
        if (snIds == null || snIds.isEmpty()) return out;
        List<SnCode> sns = snCodeMapper.selectBatchIds(snIds);
        for (SnCode sn : sns) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", sn.getId());
            m.put("snCode", sn.getSnCode());
            m.put("status", sn.getStatus());
            m.put("statusName", snStatusName(sn.getStatus()));
            // 流转时间：按 status 选择最相关的时间
            switch (sn.getStatus() == null ? -1 : sn.getStatus()) {
                case 1: m.put("time", sn.getUpdatedAt()); break;
                case 2: m.put("time", sn.getSoldAt()); break;
                case 3: m.put("time", sn.getDeliveredAt()); break;
                case 4: m.put("time", sn.getReceivedAt()); break;
                case 7: m.put("time", sn.getReturnAt()); break;
                default: m.put("time", sn.getUpdatedAt()); break;
            }
            out.add(m);
        }
        return out;
    }

    private List<Long> parseSnIds(String jsonStr) {
        if (!StringUtils.hasText(jsonStr)) return null;
        try {
            List<Long> list = objectMapper.readValue(jsonStr, objectMapper.getTypeFactory().constructCollectionType(List.class, Long.class));
            return list;
        } catch (Exception e) {
            log.warn("解析 SN ID 列表失败 json={}", jsonStr);
            return null;
        }
    }

    private String generateOrderNo() {
        return "OD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private String joinAddress(Address a) {
        return a.getProvince() + " " + a.getCity() + " " + a.getDistrict() + " " + a.getDetail();
    }
}
