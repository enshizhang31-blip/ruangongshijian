package com.salemanager.modules.statistics.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 统计数据
 */
@Data
public class DashboardStats {

    private BigDecimal todaySales;
    private BigDecimal monthSales;
    private BigDecimal totalSales;
    private Integer todayOrders;
    private Integer monthOrders;
    private Integer totalOrders;
    private Integer totalCustomers;
    private Integer totalProducts;
    private Integer totalSnCodes;
}
