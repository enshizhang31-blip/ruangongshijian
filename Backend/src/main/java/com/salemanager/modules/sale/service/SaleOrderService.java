package com.salemanager.modules.sale.service;

import com.salemanager.modules.sale.model.SaleOrder;
import com.salemanager.modules.sale.model.SaleOrderItem;
import com.salemanager.modules.sale.param.SaleOrderParam;

import java.util.List;

/**
 * 订单服务接口
 */
public interface SaleOrderService {

    /**
     * 获取订单列表
     */
    List<SaleOrder> getOrderList(String keyword, Integer status, Integer page, Integer pageSize);

    /**
     * 获取订单总数
     */
    Long getOrderCount(String keyword, Integer status);

    /**
     * 获取订单详情
     */
    SaleOrder getOrderById(Long id);

    /**
     * 获取订单明细列表
     */
    List<SaleOrderItem> getOrderItems(Long orderId);

    /**
     * 新增订单
     */
    void createOrder(SaleOrderParam param);

    /**
     * 更新订单
     */
    void updateOrder(Long id, SaleOrderParam param);

    /**
     * 删除订单
     */
    void deleteOrder(Long id);
}
