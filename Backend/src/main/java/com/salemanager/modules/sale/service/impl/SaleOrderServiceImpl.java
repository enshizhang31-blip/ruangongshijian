package com.salemanager.modules.sale.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.sale.mapper.SaleOrderItemMapper;
import com.salemanager.modules.sale.mapper.SaleOrderMapper;
import com.salemanager.modules.sale.model.SaleOrder;
import com.salemanager.modules.sale.model.SaleOrderItem;
import com.salemanager.modules.sale.param.SaleOrderParam;
import com.salemanager.modules.sale.service.SaleOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 订单服务实现
 */
@Service
public class SaleOrderServiceImpl implements SaleOrderService {

    private static final Logger log = LoggerFactory.getLogger(SaleOrderServiceImpl.class);

    @Autowired
    private SaleOrderMapper orderMapper;

    @Autowired
    private SaleOrderItemMapper orderItemMapper;

    @Override
    public List<SaleOrder> getOrderList(String keyword, Integer status, Integer page, Integer pageSize) {
        log.info("getOrderList keyword={}, status={}, page={}, pageSize={}", keyword, status, page, pageSize);

        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(SaleOrder::getOrderNo, keyword)
                    .or()
                    .like(SaleOrder::getCustomerName, keyword)
                    .or()
                    .like(SaleOrder::getCustomerPhone, keyword));
        }

        if (status != null) {
            wrapper.eq(SaleOrder::getStatus, status);
        }

        wrapper.orderByDesc(SaleOrder::getCreatedAt);

        IPage<SaleOrder> result = new Page<>(page, pageSize);
        orderMapper.selectPage(result, wrapper);

        return result.getRecords();
    }

    @Override
    public Long getOrderCount(String keyword, Integer status) {
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(SaleOrder::getOrderNo, keyword)
                    .or()
                    .like(SaleOrder::getCustomerName, keyword)
                    .or()
                    .like(SaleOrder::getCustomerPhone, keyword));
        }

        if (status != null) {
            wrapper.eq(SaleOrder::getStatus, status);
        }

        return orderMapper.selectCount(wrapper);
    }

    @Override
    public SaleOrder getOrderById(Long id) {
        log.info("getOrderById id={}", id);
        if (id == null || id <= 0) {
            throw new BusinessException(400, "订单ID无效");
        }

        SaleOrder order = orderMapper.selectById(id);
        if (order == null) {
            log.warn("订单不存在 id={}", id);
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    @Override
    public List<SaleOrderItem> getOrderItems(Long orderId) {
        log.info("getOrderItems orderId={}", orderId);
        if (orderId == null || orderId <= 0) {
            throw new BusinessException(400, "订单ID无效");
        }

        LambdaQueryWrapper<SaleOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleOrderItem::getOrderId, orderId);
        return orderItemMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void createOrder(SaleOrderParam param) {
        log.info("createOrder customerId={}", param.getCustomerId());

        SaleOrder order = new SaleOrder();
        order.setOrderNo(generateOrderNo());
        order.setCustomerId(param.getCustomerId());
        order.setCustomerName(param.getCustomerName());
        order.setCustomerPhone(param.getCustomerPhone());
        order.setPayType(param.getPayType() != null ? param.getPayType() : 1);
        order.setStatus(param.getStatus() != null ? param.getStatus() : 0);
        order.setRemark(param.getRemark());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (param.getItems() != null && !param.getItems().isEmpty()) {
            for (SaleOrderParam.OrderItemParam itemParam : param.getItems()) {
                BigDecimal itemTotal = itemParam.getPrice()
                        .multiply(BigDecimal.valueOf(itemParam.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);
            }
        }
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(totalAmount);

        orderMapper.insert(order);
        log.info("订单创建成功 orderId={}, orderNo={}", order.getId(), order.getOrderNo());

        // 保存订单明细
        if (param.getItems() != null && !param.getItems().isEmpty()) {
            for (SaleOrderParam.OrderItemParam itemParam : param.getItems()) {
                SaleOrderItem item = new SaleOrderItem();
                item.setOrderId(order.getId());
                item.setOrderNo(order.getOrderNo());
                item.setSkuId(itemParam.getSkuId());
                item.setSpuName(itemParam.getSpuName());
                item.setSkuSpec(itemParam.getSkuSpec());
                item.setSkuImage(itemParam.getSkuImage());
                item.setPrice(itemParam.getPrice());
                item.setQuantity(itemParam.getQuantity());
                item.setSubtotal(itemParam.getPrice()
                        .multiply(BigDecimal.valueOf(itemParam.getQuantity())));
                item.setCreatedAt(LocalDateTime.now());
                orderItemMapper.insert(item);
            }
        }
    }

    @Override
    @Transactional
    public void updateOrder(Long id, SaleOrderParam param) {
        log.info("updateOrder id={}", id);

        SaleOrder order = orderMapper.selectById(id);
        if (order == null) {
            log.warn("订单不存在 id={}", id);
            throw new BusinessException("订单不存在");
        }

        if (param.getCustomerName() != null) order.setCustomerName(param.getCustomerName());
        if (param.getCustomerPhone() != null) order.setCustomerPhone(param.getCustomerPhone());
        if (param.getStatus() != null) order.setStatus(param.getStatus());
        if (param.getRemark() != null) order.setRemark(param.getRemark());
        order.setUpdatedAt(LocalDateTime.now());

        orderMapper.updateById(order);
        log.info("订单更新成功 id={}", id);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        log.info("deleteOrder id={}", id);
        if (id == null || id <= 0) {
            throw new BusinessException(400, "订单ID无效");
        }

        SaleOrder order = orderMapper.selectById(id);
        if (order == null) {
            log.warn("订单不存在 id={}", id);
            throw new BusinessException("订单不存在");
        }

        orderMapper.deleteById(id);

        // 删除订单明细
        LambdaQueryWrapper<SaleOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleOrderItem::getOrderId, id);
        orderItemMapper.delete(wrapper);

        log.info("订单删除成功 id={}", id);
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
