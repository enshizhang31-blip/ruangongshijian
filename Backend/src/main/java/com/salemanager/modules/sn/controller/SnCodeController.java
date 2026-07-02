package com.salemanager.modules.sn.controller;

import com.salemanager.common.exception.BusinessException;
import com.salemanager.common.result.Result;
import com.salemanager.modules.sn.model.SnCode;
import com.salemanager.modules.sn.model.SnCodeLog;
import com.salemanager.modules.sn.param.SnCodeParam;
import com.salemanager.modules.sn.service.SnCodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

    // ====================== 扫码全流程接口 ======================

    /**
     * 通用扫码动作分发接口
     * POST /api/admin/sn/scan?action=inbound|deliver|receive|return|return-complete|void
     */
    @PostMapping("/scan")
    public Result<SnCode> scanAction(
            @RequestParam String action,
            @RequestBody Map<String, Object> params,
            HttpServletRequest request) {
        log.info("scanAction action={}", action);
        if (params == null) {
            params = new HashMap<>();
        }
        applyRealUser(params, request);

        String sn = params.get("sn") != null ? String.valueOf(params.get("sn")) : null;
        if (sn == null || sn.isEmpty()) {
            return Result.fail(400, "SN码不能为空");
        }
        try {
            SnCode result = snCodeService.scanAction(sn, action, params);
            return Result.success(result);
        } catch (BusinessException e) {
            return Result.fail(e.getCode() == null ? 500 : e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("scanAction 异常 action={}, sn={}", action, sn, e);
            return Result.fail("扫码失败: " + e.getMessage());
        }
    }

    /**
     * 扫码入库 GET（用于纯URL/扫码枪快速场景）
     * GET /api/admin/sn/scan/inbound?sn=xxx
     */
    @GetMapping("/scan/inbound")
    public Result<SnCode> scanInboundGet(@RequestParam String sn, HttpServletRequest request) {
        log.info("scanInboundGet sn={}", sn);
        try {
            Long userId = resolveUserId(request);
            SnCode result = snCodeService.scanInbound(sn, userId, null);
            return Result.success(result);
        } catch (BusinessException e) {
            return Result.fail(e.getCode() == null ? 500 : e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("scanInboundGet 异常 sn={}", sn, e);
            return Result.fail("扫码入库失败: " + e.getMessage());
        }
    }

    /**
     * 扫码入库 POST
     * POST /api/admin/sn/scan/inbound  { "sn": "xxx" }
     */
    @PostMapping("/scan/inbound")
    public Result<SnCode> scanInboundPost(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        log.info("scanInboundPost params={}", params);
        if (params == null) {
            params = new HashMap<>();
        }
        applyRealUser(params, request);
        String sn = params.get("sn") != null ? String.valueOf(params.get("sn")) : null;
        if (sn == null || sn.isEmpty()) {
            return Result.fail(400, "SN码不能为空");
        }
        try {
            Long userId = toLong(params.get("userId"));
            String userName = params.get("userName") != null ? String.valueOf(params.get("userName")) : null;
            SnCode result = snCodeService.scanInbound(sn, userId, userName);
            return Result.success(result);
        } catch (BusinessException e) {
            return Result.fail(e.getCode() == null ? 500 : e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("scanInboundPost 异常 sn={}", sn, e);
            return Result.fail("扫码入库失败: " + e.getMessage());
        }
    }

    /**
     * 扫码发货
     * POST /api/admin/sn/scan/deliver  { "sn": "xxx", "logisticsNo": "xxx" }
     */
    @PostMapping("/scan/deliver")
    public Result<SnCode> scanDeliver(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        log.info("scanDeliver params={}", params);
        if (params == null) {
            params = new HashMap<>();
        }
        applyRealUser(params, request);
        String sn = params.get("sn") != null ? String.valueOf(params.get("sn")) : null;
        if (sn == null || sn.isEmpty()) {
            return Result.fail(400, "SN码不能为空");
        }
        String logisticsNo = params.get("logisticsNo") != null ? String.valueOf(params.get("logisticsNo")) : null;
        try {
            Long userId = toLong(params.get("userId"));
            String userName = params.get("userName") != null ? String.valueOf(params.get("userName")) : null;
            SnCode result = snCodeService.scanDeliver(sn, logisticsNo, userId, userName);
            return Result.success(result);
        } catch (BusinessException e) {
            return Result.fail(e.getCode() == null ? 500 : e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("scanDeliver 异常 sn={}", sn, e);
            return Result.fail("扫码发货失败: " + e.getMessage());
        }
    }

    /**
     * 扫码签收
     * POST /api/admin/sn/scan/receive  { "sn": "xxx" }
     */
    @PostMapping("/scan/receive")
    public Result<SnCode> scanReceive(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        log.info("scanReceive params={}", params);
        if (params == null) {
            params = new HashMap<>();
        }
        applyRealUser(params, request);
        String sn = params.get("sn") != null ? String.valueOf(params.get("sn")) : null;
        if (sn == null || sn.isEmpty()) {
            return Result.fail(400, "SN码不能为空");
        }
        try {
            Long userId = toLong(params.get("userId"));
            String userName = params.get("userName") != null ? String.valueOf(params.get("userName")) : null;
            SnCode result = snCodeService.scanReceive(sn, userId, userName);
            return Result.success(result);
        } catch (BusinessException e) {
            return Result.fail(e.getCode() == null ? 500 : e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("scanReceive 异常 sn={}", sn, e);
            return Result.fail("扫码签收失败: " + e.getMessage());
        }
    }

    /**
     * 扫码退货
     * POST /api/admin/sn/scan/return  { "sn": "xxx", "reason": "xxx" }
     */
    @PostMapping("/scan/return")
    public Result<SnCode> scanReturn(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        log.info("scanReturn params={}", params);
        if (params == null) {
            params = new HashMap<>();
        }
        applyRealUser(params, request);
        String sn = params.get("sn") != null ? String.valueOf(params.get("sn")) : null;
        if (sn == null || sn.isEmpty()) {
            return Result.fail(400, "SN码不能为空");
        }
        String reason = params.get("reason") != null ? String.valueOf(params.get("reason")) : null;
        try {
            SnCode result = snCodeService.scanReturn(sn, reason);
            return Result.success(result);
        } catch (BusinessException e) {
            return Result.fail(e.getCode() == null ? 500 : e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("scanReturn 异常 sn={}", sn, e);
            return Result.fail("扫码退货失败: " + e.getMessage());
        }
    }

    /**
     * 扫码退货完成（退货入库）
     * POST /api/admin/sn/scan/return-complete  { "sn": "xxx" }
     */
    @PostMapping("/scan/return-complete")
    public Result<SnCode> scanReturnComplete(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        log.info("scanReturnComplete params={}", params);
        if (params == null) {
            params = new HashMap<>();
        }
        applyRealUser(params, request);
        String sn = params.get("sn") != null ? String.valueOf(params.get("sn")) : null;
        if (sn == null || sn.isEmpty()) {
            return Result.fail(400, "SN码不能为空");
        }
        try {
            Long userId = toLong(params.get("userId"));
            String userName = params.get("userName") != null ? String.valueOf(params.get("userName")) : null;
            SnCode result = snCodeService.scanReturnComplete(sn, userId, userName);
            return Result.success(result);
        } catch (BusinessException e) {
            return Result.fail(e.getCode() == null ? 500 : e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("scanReturnComplete 异常 sn={}", sn, e);
            return Result.fail("退货入库失败: " + e.getMessage());
        }
    }

    /**
     * 扫码作废
     * POST /api/admin/sn/scan/void  { "sn": "xxx", "reason": "xxx" }
     */
    @PostMapping("/scan/void")
    public Result<SnCode> scanVoid(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        log.info("scanVoid params={}", params);
        if (params == null) {
            params = new HashMap<>();
        }
        applyRealUser(params, request);
        String sn = params.get("sn") != null ? String.valueOf(params.get("sn")) : null;
        if (sn == null || sn.isEmpty()) {
            return Result.fail(400, "SN码不能为空");
        }
        String reason = params.get("reason") != null ? String.valueOf(params.get("reason")) : null;
        try {
            SnCode result = snCodeService.scanVoid(sn, reason);
            return Result.success(result);
        } catch (BusinessException e) {
            return Result.fail(e.getCode() == null ? 500 : e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("scanVoid 异常 sn={}", sn, e);
            return Result.fail("扫码作废失败: " + e.getMessage());
        }
    }

    /**
     * 批量扫码入库
     * POST /api/admin/sn/scan/batch-inbound  { "sns": ["xxx","yyy"] }
     */
    @PostMapping("/scan/batch-inbound")
    public Result<Map<String, Object>> batchScanInbound(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        log.info("batchScanInbound params={}", params);
        if (params == null) {
            params = new HashMap<>();
        }
        applyRealUser(params, request);
        Object snsObj = params.get("sns");
        List<String> sns = new ArrayList<>();
        if (snsObj instanceof List) {
            for (Object o : (List<?>) snsObj) {
                if (o != null) {
                    sns.add(String.valueOf(o));
                }
            }
        } else if (snsObj != null) {
            // 兼容单字符串或逗号分隔
            String s = String.valueOf(snsObj);
            if (s.contains(",")) {
                for (String item : s.split(",")) {
                    if (!item.trim().isEmpty()) {
                        sns.add(item.trim());
                    }
                }
            } else if (!s.trim().isEmpty()) {
                sns.add(s.trim());
            }
        }
        if (sns.isEmpty()) {
            return Result.fail(400, "SN码列表不能为空");
        }
        try {
            Long userId = toLong(params.get("userId"));
            String userName = params.get("userName") != null ? String.valueOf(params.get("userName")) : null;
            Map<String, Object> result = snCodeService.batchScanInbound(sns, userId, userName);
            return Result.success(result);
        } catch (BusinessException e) {
            return Result.fail(e.getCode() == null ? 500 : e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("batchScanInbound 异常", e);
            return Result.fail("批量扫码入库失败: " + e.getMessage());
        }
    }

    /**
     * 扫码解析（URL/二维码内容解析出 SN 等信息）
     * GET /api/admin/sn/scan/parse?url=xxx
     */
    @GetMapping("/scan/parse")
    public Result<Map<String, Object>> scanParse(@RequestParam String url) {
        log.info("scanParse url={}", url);
        Map<String, Object> result = new HashMap<>();
        result.put("raw", url);
        String sn = url;
        if (url != null) {
            // 尝试解析常见二维码格式
            // 格式1: 直接SN码
            // 格式2: "sn=xxx" 形式
            // 格式3: URL路径中包含SN码，如 /sn/xxx 或 /code/xxx
            int idx = url.indexOf("sn=");
            if (idx >= 0) {
                String tail = url.substring(idx + 3);
                int amp = tail.indexOf('&');
                sn = amp > 0 ? tail.substring(0, amp) : tail;
            } else {
                int slashIdx = url.lastIndexOf('/');
                if (slashIdx >= 0 && slashIdx < url.length() - 1) {
                    sn = url.substring(slashIdx + 1);
                }
            }
        }
        result.put("sn", sn);
        return Result.success(result);
    }

    // ====================== 私有辅助方法 ======================

    /**
     * 从 request 中取出真实 userId 覆盖 params 中的 userId/userName
     * 优先使用 JWT 过滤器写入的 userId，避免前端伪造
     */
    private void applyRealUser(Map<String, Object> params, HttpServletRequest request) {
        if (request == null || params == null) {
            return;
        }
        Object attr = request.getAttribute("userId");
        if (attr != null) {
            Long realUserId = null;
            if (attr instanceof Long) {
                realUserId = (Long) attr;
            } else if (attr instanceof Number) {
                realUserId = ((Number) attr).longValue();
            } else {
                try {
                    realUserId = Long.parseLong(attr.toString());
                } catch (Exception ignore) {
                }
            }
            if (realUserId != null) {
                params.put("userId", realUserId);
                // 通过服务端解析真实姓名
                try {
                    String realName = snCodeService.getUserNameById(realUserId);
                    if (realName != null && !realName.isEmpty()) {
                        params.put("userName", realName);
                    }
                } catch (Exception ignore) {
                }
            }
        }
    }

    private Long resolveUserId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object attr = request.getAttribute("userId");
        return toLong(attr);
    }

    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Long) return (Long) o;
        if (o instanceof Number) return ((Number) o).longValue();
        try {
            return Long.parseLong(o.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
