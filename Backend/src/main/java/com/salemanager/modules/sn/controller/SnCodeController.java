package com.salemanager.modules.sn.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.sn.model.SnCode;
import com.salemanager.modules.sn.model.SnCodeLog;
import com.salemanager.modules.sn.param.SnCodeParam;
import com.salemanager.modules.sn.service.SnCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SN码控制器
 */
@RestController
@RequestMapping("/api/admin/sn")
public class SnCodeController {

    @Autowired
    private SnCodeService snCodeService;

    /**
     * 获取SN码列表
     */
    @GetMapping("/code")
    public Result<Map<String, Object>> getSnCodeList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        List<SnCode> list = snCodeService.getSnCodeList(keyword, status, page, pageSize);
        Long total = snCodeService.getSnCodeCount(keyword, status);

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
     * 获取SN码详情
     */
    @GetMapping("/code/{id}")
    public Result<SnCode> getSnCodeById(@PathVariable Long id) {
        SnCode snCode = snCodeService.getSnCodeById(id);
        return Result.success(snCode);
    }

    /**
     * 录入SN码
     */
    @PostMapping("/code")
    public Result<Void> createSnCode(@RequestBody SnCodeParam param) {
        snCodeService.createSnCode(param);
        return Result.success();
    }

    /**
     * 批量录入SN码
     */
    @PostMapping("/code/batch")
    public Result<Map<String, Object>> batchCreateSnCode(@RequestBody SnCodeParam param) {
        Map<String, Object> result = snCodeService.batchCreateSnCode(param);
        return Result.success(result);
    }

    /**
     * 绑定SN码到订单
     */
    @PostMapping("/code/bind")
    public Result<Void> bindSnCode(@RequestBody Map<String, Object> params) {
        String sn = (String) params.get("sn");
        Long orderId = params.get("orderId") != null ? Long.valueOf(params.get("orderId").toString()) : null;
        snCodeService.bindSnCode(sn, orderId);
        return Result.success();
    }

    /**
     * 解绑SN码
     */
    @PostMapping("/code/{id}/unbind")
    public Result<Void> unbindSnCode(@PathVariable Long id) {
        snCodeService.unbindSnCode(id);
        return Result.success();
    }

    /**
     * 查询SN码
     */
    @GetMapping("/code/query/{sn}")
    public Result<SnCode> querySnCode(@PathVariable String sn) {
        SnCode snCode = snCodeService.getSnCodeBySn(sn);
        return Result.success(snCode);
    }

    /**
     * 获取SN码操作日志
     */
    @GetMapping("/log")
    public Result<Map<String, Object>> getSnCodeLogs(
            @RequestParam(required = false) Long snId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        List<SnCodeLog> list = snCodeService.getSnCodeLogs(snId, page, pageSize);
        Long total = snCodeService.getSnCodeLogCount(snId);

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
     * 按商品ID获取SN码列表
     */
    @GetMapping("/code/goods/{goodsId}")
    public Result<List<SnCode>> getSnCodesByGoodsId(@PathVariable Long goodsId) {
        List<SnCode> list = snCodeService.getSnCodesByGoodsId(goodsId);
        return Result.success(list);
    }
}
