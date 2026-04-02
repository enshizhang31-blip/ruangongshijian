package com.salemanager.modules.statistics.service;

import com.salemanager.modules.statistics.model.DashboardStats;

/**
 * 统计服务接口
 */
public interface DashboardService {

    /**
     * 获取统计数据
     */
    DashboardStats getStats();
}
