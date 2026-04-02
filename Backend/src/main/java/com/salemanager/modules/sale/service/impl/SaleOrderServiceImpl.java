package com.salemanager.modules.sale.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salemanager.modules.sale.mapper.SaleOrderItemMapper;
import com.salemanager.modules.sale.mapper.SaleOrderMapper;
import com.salemanager.modules.sale.model.SaleOrder;
import com.salemanager.modules.sale.model.SaleOrderItem;
import com.salemanager.modules.sale.param.SaleOrderParam;
import com.salemanager.modules.sale.service.SaleOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 订单服务实现
 */
@Service
public class SaleOrderServiceImpl implements SaleOrderService {

    @Autowired
    private SaleOrderMapper orderMapper;

    @Autowired
    private SaleOrderItemMapper orderItemMapper;

    @Override
    public List<SaleOrder> getOrderList(String keyword, Integer status, Integer page, Integer pageSize) {
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
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

        int offset = (page - 1) * pageSize;
        wrapper.last("LIMIT " + offset + ", " + pageSize);

        return orderMapper.selectList(wrapper);
    }

    @Override
    public Long getOrderCount(String keyword, Integer status) {
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
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
        return orderMapper.selectById(id);
    }

    @Override
    public List<SaleOrderItem> getOrderItems(Long orderId) {
        LambdaQueryWrapper<SaleOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleOrderItem::getOrderId, orderId);
        return orderItemMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void createOrder(SaleOrderParam param) {
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
        if (param.getItems() != null) {
            for (SaleOrderParam.OrderItemParam itemParam : param.getItems()) {
                BigDecimal itemTotal = BigDecimal.valueOf(itemParam.getPrice()).multiply(BigDecimal.valueOf(itemParam.getQuantity()));
                totalAmount = totalAmount.add(itemTotal);
            }
        }
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(totalAmount);

        orderMapper.insert(order);

        // 保存订单明细
        if (param.getItems() != null) {
            for (SaleOrderParam.OrderItemParam itemParam : param.getItems()) {
                SaleOrderItem item = new SaleOrderItem();
                item.setOrderId(order.getId());
                item.setOrderNo(order.getOrderNo());
                item.setSkuId(itemParam.getSkuId());
                item.setSpuName(itemParam.getSpuName());
                item.setSkuSpec(itemParam.getSkuSpec());
                item.setSkuImage(itemParam.getSkuImage());
                item.setPrice(BigDecimal.valueOf(itemParam.getPrice()));
                item.setQuantity(itemParam.getQuantity());
                item.setSubtotal(BigDecimal.valueOf(itemParam.getPrice()).multiply(BigDecimal.valueOf(itemParam.getQuantity())));
                item.setCreatedAt(LocalDateTime.now());
                orderItemMapper.insert(item);
            }
        }
    }

    @Override
    public void updateOrder(Long id, SaleOrderParam param) {
        SaleOrder order = orderMapper.selectById(id);
        if (order == null) {
            return;
        }

        if (param.getCustomerName() != null) order.setCustomerName(param.getCustomerName());
        if (param.getCustomerPhone() != null) order.setCustomerPhone(param.getCustomerPhone());
        if (param.getStatus() != null) order.setStatus(param.getStatus());
        if (param.getRemark() != null) order.setRemark(param.getRemark());
        order.setUpdatedAt(LocalDateTime.now());

        orderMapper.updateById(order);
    }

    @Override
    public void deleteOrder(Long id) {
        orderMapper.deleteById(id);
        // 删除订单明细
        LambdaQueryWrapper<SaleOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleOrderItem::getOrderId, id);
        orderItemMapper.delete(wrapper);
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
