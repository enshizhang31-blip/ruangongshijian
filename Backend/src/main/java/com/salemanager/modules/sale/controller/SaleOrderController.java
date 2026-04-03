package com.salemanager.modules.sale.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.sale.model.SaleOrder;
import com.salemanager.modules.sale.model.SaleOrderItem;
import com.salemanager.modules.sale.param.SaleOrderParam;
import com.salemanager.modules.sale.service.SaleOrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/admin/sale/order")
@Validated
public class SaleOrderController {

    private static final Logger log = LoggerFactory.getLogger(SaleOrderController.class);

    @Autowired
    private SaleOrderService saleOrderService;

    /**
     * 获取订单列表
     */
    @GetMapping
    public Result<Map<String, Object>> getOrderList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        log.info("getOrderList keyword={}, status={}, page={}, pageSize={}", keyword, status, page, pageSize);

        List<SaleOrder> list = saleOrderService.getOrderList(keyword, status, page, pageSize);
        Long total = saleOrderService.getOrderCount(keyword, status);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("pagination", Map.of(
                "page", page,
                "pageSize", pageSize,
                "total", total
        ));

        return Result.success(result);
    }

    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Result<SaleOrder> getOrderById(@PathVariable Long id) {
        log.info("getOrderById id={}", id);
        SaleOrder order = saleOrderService.getOrderById(id);
        return Result.success(order);
    }

    /**
     * 获取订单明细列表
     */
    @GetMapping("/{id}/items")
    public Result<List<SaleOrderItem>> getOrderItems(@PathVariable Long id) {
        log.info("getOrderItems orderId={}", id);
        List<SaleOrderItem> items = saleOrderService.getOrderItems(id);
        return Result.success(items);
    }

    /**
     * 新增订单
     */
    @PostMapping
    public Result<Void> createOrder(@Valid @RequestBody SaleOrderParam param) {
        log.info("createOrder customerId={}", param.getCustomerId());
        saleOrderService.createOrder(param);
        return Result.success();
    }

    /**
     * 更新订单
     */
    @PutMapping("/{id}")
    public Result<Void> updateOrder(@PathVariable Long id, @Valid @RequestBody SaleOrderParam param) {
        log.info("updateOrder id={}", id);
        saleOrderService.updateOrder(id, param);
        return Result.success();
    }

    /**
     * 删除订单
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteOrder(@PathVariable Long id) {
        log.info("deleteOrder id={}", id);
        saleOrderService.deleteOrder(id);
        return Result.success();
    }
}
