package com.salemanager.modules.statistics.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.statistics.model.DashboardStats;
import com.salemanager.modules.statistics.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 统计控制器
 */
@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    /**
     * 获取统计数据
     */
    @GetMapping("/stats")
    public Result<DashboardStats> getStats() {
        log.info("getStats");
        DashboardStats stats = dashboardService.getStats();
        return Result.success(stats);
    }
}
