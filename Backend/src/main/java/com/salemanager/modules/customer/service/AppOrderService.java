package com.salemanager.modules.customer.service;

import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.sale.model.SaleOrder;
import com.salemanager.modules.sale.model.SaleOrderItem;

import java.util.List;
import java.util.Map;

public interface AppOrderService {

    /**
     * 创建订单（下单时锁定 SN：sn.status 0 → 1）。
     *
     * @param param 包含 addressId 与 items
     * @return orderId
     */
    Long createOrder(Long customerId, CreateOrderParam param);

    /**
     * 查询订单列表（含 items）。
     */
    List<Map<String, Object>> listByCustomer(Long customerId, Integer status, Integer page, Integer pageSize);

    /**
     * 订单详情。
     */
    Map<String, Object> getDetail(Long customerId, Long orderId);

    /**
     * 支付（下单锁定后支付 → sn.status 1 → 2 已售，order.status 0 → 2 已发货 → mock 自动到 1 已支付）。
     *
     * 这里演示版流程：订单状态变化顺序 0待支付 → 1已支付 → 2已发货（mock）。SN 状态 1锁定 → 2已售。
     */
    void payOrder(Long customerId, Long orderId, Integer payType);

    /**
     * 取消订单。SN 由 1 回到 0。
     */
    void cancelOrder(Long customerId, Long orderId);

    /**
     * 模拟发货 → 签收（演示版省去物流）。
     */
    void deliverThenReceive(Long customerId, Long orderId);

    /**
     * 演示版「商家发货」：订单 1→2，SN 2→3（已发货）。
     */
    void shipOrder(Long customerId, Long orderId);

    /**
     * 演示版「退款完成」：订单 6→7，SN 7→8（已退货）。
     */
    void manualCompleteRefund(Long customerId, Long orderId);

    /**
     * 申请退款。
     */
    Long applyRefund(Long customerId, Long orderId, String reason);

    class CreateOrderParam {
        public Long addressId;
        public List<Item> items;
    }
    class Item {
        public Long spuId;
        public Long skuId;
        public Integer quantity;
    }
}
