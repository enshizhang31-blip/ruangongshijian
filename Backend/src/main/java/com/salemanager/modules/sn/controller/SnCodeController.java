package com.salemanager.modules.sn.controller;

import com.salemanager.common.result.Result;
import com.salemanager.modules.sn.model.SnCode;
import com.salemanager.modules.sn.model.SnCodeLog;
import com.salemanager.modules.sn.param.SnCodeParam;
import com.salemanager.modules.sn.service.SnCodeService;
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
 * SN码控制器
 */
@RestController
@RequestMapping("/api/admin/sn")
@Validated
public class SnCodeController {

    private static final Logger log = LoggerFactory.getLogger(SnCodeController.class);

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

        log.info("getSnCodeList keyword={}, status={}, page={}, pageSize={}", keyword, status, page, pageSize);

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
        log.info("getSnCodeById id={}", id);
        SnCode snCode = snCodeService.getSnCodeById(id);
        return Result.success(snCode);
    }

    /**
     * 录入SN码
     */
    @PostMapping("/code")
    public Result<Void> createSnCode(@Valid @RequestBody SnCodeParam param) {
        log.info("createSnCode sn={}", param.getSn());
        snCodeService.createSnCode(param);
        return Result.success();
    }

    /**
     * 批量录入SN码
     */
    @PostMapping("/code/batch")
    public Result<Map<String, Object>> batchCreateSnCode(@Valid @RequestBody SnCodeParam param) {
        log.info("batchCreateSnCode count={}", param.getSns() != null ? param.getSns().length : 0);
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
        log.info("bindSnCode sn={}, orderId={}", sn, orderId);
        snCodeService.bindSnCode(sn, orderId);
        return Result.success();
    }

    /**
     * 解绑SN码
     */
    @PostMapping("/code/{id}/unbind")
    public Result<Void> unbindSnCode(@PathVariable Long id) {
        log.info("unbindSnCode id={}", id);
        snCodeService.unbindSnCode(id);
        return Result.success();
    }

    /**
     * 查询SN码
     */
    @GetMapping("/code/query/{sn}")
    public Result<SnCode> querySnCode(@PathVariable String sn) {
        log.info("querySnCode sn={}", sn);
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

        log.info("getSnCodeLogs snId={}, page={}, pageSize={}", snId, page, pageSize);

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
        log.info("getSnCodesByGoodsId goodsId={}", goodsId);
        List<SnCode> list = snCodeService.getSnCodesByGoodsId(goodsId);
        return Result.success(list);
    }

    /**
     * 按SKU ID获取SN码列表
     */
    @GetMapping("/code/sku/{skuId}")
    public Result<Map<String, Object>> getSnCodesBySkuId(
            @PathVariable Long skuId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("getSnCodesBySkuId skuId={}, page={}, pageSize={}", skuId, page, pageSize);
        List<SnCode> list = snCodeService.getSnCodesBySkuId(skuId, page, pageSize);
        Long total = snCodeService.getSnCodeCountBySkuId(skuId);
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("pagination", Map.of("page", page, "pageSize", pageSize, "total", total));
        return Result.success(result);
    }

    /**
     * 获取SKU的SN码状态统计
     */
    @GetMapping("/code/sku/{skuId}/stats")
    public Result<Map<String, Long>> getSnCodeStatsBySkuId(@PathVariable Long skuId) {
        log.info("getSnCodeStatsBySkuId skuId={}", skuId);
        Map<String, Long> stats = snCodeService.getSnCodeStatsBySkuId(skuId);
        return Result.success(stats);
    }

    /**
     * 作废SN码
     */
    @PostMapping("/code/{id}/void")
    public Result<Void> voidSnCode(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body != null ? body.get("remark") : null;
        log.info("voidSnCode id={}", id);
        snCodeService.voidSnCode(id, remark);
        return Result.success();
    }

    /**
     * 退货申请（已售→退货中）
     */
    @PostMapping("/code/{id}/return")
    public Result<Void> applyReturnSnCode(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body != null ? body.get("remark") : null;
        log.info("applyReturnSnCode id={}", id);
        snCodeService.applyReturnSnCode(id, remark);
        return Result.success();
    }

    /**
     * 退货完成（退货中→已退货，重新入库）
     */
    @PostMapping("/code/{id}/return-complete")
    public Result<Void> completeReturnSnCode(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String remark = body != null ? body.get("remark") : null;
        log.info("completeReturnSnCode id={}", id);
        snCodeService.completeReturnSnCode(id, remark);
        return Result.success();
    }

    /**
     * 自动生成SN码（基于SKU编码+序号）
     */
    @PostMapping("/code/generate")
    public Result<List<SnCode>> generateSnCodes(@RequestBody Map<String, Object> params) {
        Long skuId = params.get("skuId") != null ? Long.valueOf(params.get("skuId").toString()) : null;
        int count = params.get("count") != null ? Integer.parseInt(params.get("count").toString()) : 1;
        log.info("generateSnCodes skuId={}, count={}", skuId, count);
        List<SnCode> list = snCodeService.generateSnCodes(skuId, count);
        return Result.success(list);
    }

    /**
     * 更新SN码状态
     */
    @PutMapping("/code/{id}/status")
    public Result<Void> updateSnCodeStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        log.info("updateSnCodeStatus id={}, status={}", id, status);
        snCodeService.updateSnCodeStatus(id, status);
        return Result.success();
    }
}
