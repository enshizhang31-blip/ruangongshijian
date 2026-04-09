package com.salemanager.modules.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.salemanager.modules.customer.mapper.CustomerMapper;
import com.salemanager.modules.product.mapper.GoodsMapper;
import com.salemanager.modules.product.model.Goods;
import com.salemanager.modules.sale.mapper.SaleOrderMapper;
import com.salemanager.modules.sale.model.SaleOrder;
import com.salemanager.modules.sn.mapper.SnCodeMapper;
import com.salemanager.modules.sn.model.SnCode;
import com.salemanager.modules.statistics.model.DashboardStats;
import com.salemanager.modules.statistics.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * 统计服务实现
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardServiceImpl.class);

    @Autowired
    private SaleOrderMapper orderMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private SnCodeMapper snCodeMapper;

    @Override
    public DashboardStats getStats() {
        log.info("getStats");
        DashboardStats stats = new DashboardStats();

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();

        // 今日销售额
        BigDecimal todaySales = getSalesAmount(todayStart, todayEnd);
        stats.setTodaySales(todaySales);

        // 本月销售额
        BigDecimal monthSales = getSalesAmount(monthStart, todayEnd);
        stats.setMonthSales(monthSales);

        // 总销售额
        BigDecimal totalSales = getSalesAmount(null, todayEnd);
        stats.setTotalSales(totalSales);

        // 今日订单数
        stats.setTodayOrders(getOrderCount(todayStart, todayEnd));

        // 本月订单数
        stats.setMonthOrders(getOrderCount(monthStart, todayEnd));

        // 订单总数
        stats.setTotalOrders(getOrderCount(null, todayEnd));

        // 客户总数
        stats.setTotalCustomers(customerMapper.selectCount(null).intValue());

        // 商品总数
        stats.setTotalProducts(goodsMapper.selectCount(null).intValue());

        // SN码总数
        stats.setTotalSnCodes(snCodeMapper.selectCount(null).intValue());

        return stats;
    }

    private BigDecimal getSalesAmount(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleOrder::getStatus, 1); // 已付款
        wrapper.select(SaleOrder::getPayAmount);

        if (start != null) {
            wrapper.ge(SaleOrder::getCreatedAt, start);
        }
        wrapper.le(SaleOrder::getCreatedAt, end);

        var orders = orderMapper.selectList(wrapper);
        return orders.stream()
                .map(SaleOrder::getPayAmount)
        .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Integer getOrderCount(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SaleOrder::getStatus, 1); // 已付款

        if (start != null) {
            wrapper.ge(SaleOrder::getCreatedAt, start);
        }
        wrapper.le(SaleOrder::getCreatedAt, end);

        return orderMapper.selectCount(wrapper).intValue();
    }
}
