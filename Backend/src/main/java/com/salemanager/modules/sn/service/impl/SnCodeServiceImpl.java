package com.salemanager.modules.sn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.product.mapper.GoodsMapper;
import com.salemanager.modules.product.mapper.SkuMapper;
import com.salemanager.modules.product.model.Goods;
import com.salemanager.modules.product.model.Sku;
import com.salemanager.modules.sn.mapper.SnCodeLogMapper;
import com.salemanager.modules.sn.mapper.SnCodeMapper;
import com.salemanager.modules.sn.model.SnCode;
import com.salemanager.modules.sn.model.SnCodeLog;
import com.salemanager.modules.sn.param.SnCodeParam;
import com.salemanager.modules.sn.service.SnCodeService;
import com.salemanager.modules.ums.mapper.AdminUserMapper;
import com.salemanager.modules.ums.model.AdminUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SN码服务实现
 */
@Service
public class SnCodeServiceImpl implements SnCodeService {

    private static final Logger log = LoggerFactory.getLogger(SnCodeServiceImpl.class);

    @Autowired
    private SnCodeMapper snCodeMapper;

    @Autowired
    private SnCodeLogMapper snCodeLogMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Override
    public List<SnCode> getSnCodeList(String keyword, Integer status, Integer page, Integer pageSize) {
        log.info("getSnCodeList keyword={}, status={}, page={}, pageSize={}", keyword, status, page, pageSize);

        LambdaQueryWrapper<SnCode> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(SnCode::getSnCode, keyword)
                    .or()
                    .like(SnCode::getSpuName, keyword)
                    .or()
                    .like(SnCode::getSkuCode, keyword));
        }

        if (status != null) {
            wrapper.eq(SnCode::getStatus, status);
        }

        wrapper.orderByDesc(SnCode::getCreatedAt);

        IPage<SnCode> result = new Page<>(page, pageSize);
        snCodeMapper.selectPage(result, wrapper);

        return result.getRecords();
    }

    @Override
    public Long getSnCodeCount(String keyword, Integer status) {
        LambdaQueryWrapper<SnCode> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(SnCode::getSnCode, keyword)
                    .or()
                    .like(SnCode::getSpuName, keyword)
                    .or()
                    .like(SnCode::getSkuCode, keyword));
        }

        if (status != null) {
            wrapper.eq(SnCode::getStatus, status);
        }

        return snCodeMapper.selectCount(wrapper);
    }

    @Override
    public SnCode getSnCodeById(Long id) {
        log.info("getSnCodeById id={}", id);
        if (id == null || id <= 0) {
            throw new BusinessException(400, "SN码ID无效");
        }

        SnCode snCode = snCodeMapper.selectById(id);
        if (snCode == null) {
            log.warn("SN码不存在 id={}", id);
            throw new BusinessException("SN码不存在");
        }
        return snCode;
    }

    @Override
    public SnCode getSnCodeBySn(String sn) {
        log.info("getSnCodeBySn sn={}", sn);
        if (!StringUtils.hasText(sn)) {
            throw new BusinessException(400, "SN码不能为空");
        }

        LambdaQueryWrapper<SnCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SnCode::getSnCode, sn);
        SnCode result = snCodeMapper.selectOne(wrapper);

        if (result == null) {
            log.warn("SN码不存在 sn={}", sn);
            throw new BusinessException("SN码不存在");
        }
        return result;
    }

    @Override
    @Transactional
    public void createSnCode(SnCodeParam param) {
        log.info("createSnCode sn={}", param.getSn());

        SnCode snCode = new SnCode();
        snCode.setSnCode(param.getSn());
        snCode.setSkuId(param.getSkuId());
        snCode.setStatus(0); // 在库
        snCode.setSource(1); // 手动录入
        snCode.setCreatedAt(LocalDateTime.now());
        snCode.setUpdatedAt(LocalDateTime.now());

        // 填充商品信息
        if (param.getGoodsId() != null) {
            Goods goods = goodsMapper.selectById(param.getGoodsId());
            if (goods != null) {
                snCode.setSpuId(goods.getId());
                snCode.setSpuName(goods.getName());
            }
        }
        if (param.getSkuId() != null) {
            Sku sku = skuMapper.selectById(param.getSkuId());
            if (sku != null) {
                snCode.setSpuId(sku.getSpuId());
                snCode.setSkuCode(sku.getSkuCode());
                snCode.setSpecJson(sku.getSpecJson());
                snCode.setPrice(sku.getPrice());
            }
        }

        snCodeMapper.insert(snCode);
        log.info("SN码创建成功 id={}, sn={}", snCode.getId(), snCode.getSnCode());

        // 记录日志
        logOperation(snCode.getId(), snCode.getSnCode(), param.getSkuId(), "录入", null, 0, null, param.getRemark());
    }

    @Override
    @Transactional
    public Map<String, Object> batchCreateSnCode(SnCodeParam param) {
        log.info("batchCreateSnCode goodsId={}, count={}", param.getGoodsId(),
                param.getSns() != null ? param.getSns().length : 0);

        int success = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        if (param.getSns() != null && param.getSns().length > 0) {
            for (String sn : param.getSns()) {
                try {
                    SnCodeParam singleParam = new SnCodeParam();
                    singleParam.setSn(sn);
                    singleParam.setGoodsId(param.getGoodsId());
                    singleParam.setSkuId(param.getSkuId());
                    singleParam.setRemark(param.getRemark());
                    createSnCode(singleParam);
                    success++;
                } catch (Exception e) {
                    failed++;
                    errors.add(sn + ": " + e.getMessage());
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("failed", failed);
        result.put("errors", errors);
        log.info("batchCreateSnCode 完成 success={}, failed={}", success, failed);
        return result;
    }

    @Override
    @Transactional
    public void bindSnCode(String sn, Long orderId) {
        log.info("bindSnCode sn={}, orderId={}", sn, orderId);

        SnCode snCode = getSnCodeBySn(sn);
        Integer oldStatus = snCode.getStatus();

        LambdaUpdateWrapper<SnCode> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SnCode::getId, snCode.getId())
                .set(SnCode::getStatus, 1)
                .set(SnCode::getSoldAt, LocalDateTime.now())
                .set(SnCode::getUpdatedAt, LocalDateTime.now());
        snCodeMapper.update(null, wrapper);

        log.info("SN码绑定成功 sn={}", sn);
        logOperation(snCode.getId(), snCode.getSnCode(), snCode.getSkuId(), "销售", oldStatus, 1, null, "订单ID: " + orderId);
    }

    @Override
    @Transactional
    public void unbindSnCode(Long id) {
        log.info("unbindSnCode id={}", id);

        SnCode snCode = snCodeMapper.selectById(id);
        if (snCode == null) {
            log.warn("SN码不存在 id={}", id);
            throw new BusinessException("SN码不存在");
        }

        Integer oldStatus = snCode.getStatus();

        LambdaUpdateWrapper<SnCode> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SnCode::getId, id)
                .set(SnCode::getStatus, 0)
                .set(SnCode::getUpdatedAt, LocalDateTime.now());
        snCodeMapper.update(null, wrapper);

        log.info("SN码解绑成功 id={}", id);
        logOperation(snCode.getId(), snCode.getSnCode(), snCode.getSkuId(), "解绑", oldStatus, 0, null, null);
    }

    @Override
    public List<SnCodeLog> getSnCodeLogs(Long snId, Integer page, Integer pageSize) {
        log.info("getSnCodeLogs snId={}, page={}, pageSize={}", snId, page, pageSize);

        LambdaQueryWrapper<SnCodeLog> wrapper = new LambdaQueryWrapper<>();

        if (snId != null) {
            wrapper.eq(SnCodeLog::getSnCodeId, snId);
        }

        wrapper.orderByDesc(SnCodeLog::getCreatedAt);

        IPage<SnCodeLog> result = new Page<>(page, pageSize);
        snCodeLogMapper.selectPage(result, wrapper);

        return result.getRecords();
    }

    @Override
    public Long getSnCodeLogCount(Long snId) {
        LambdaQueryWrapper<SnCodeLog> wrapper = new LambdaQueryWrapper<>();

        if (snId != null) {
            wrapper.eq(SnCodeLog::getSnCodeId, snId);
        }

        return snCodeLogMapper.selectCount(wrapper);
    }

    @Override
    public List<SnCode> getSnCodesByGoodsId(Long goodsId) {
        log.info("getSnCodesByGoodsId goodsId={}", goodsId);

        LambdaQueryWrapper<SnCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SnCode::getSpuId, goodsId);
        wrapper.orderByDesc(SnCode::getCreatedAt);
        return snCodeMapper.selectList(wrapper);
    }

    @Override
    public List<SnCode> getSnCodesBySkuId(Long skuId, Integer page, Integer pageSize) {
        log.info("getSnCodesBySkuId skuId={}, page={}, pageSize={}", skuId, page, pageSize);
        if (skuId == null || skuId <= 0) {
            throw new BusinessException(400, "SKU ID无效");
        }

        LambdaQueryWrapper<SnCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SnCode::getSkuId, skuId);
        wrapper.orderByDesc(SnCode::getCreatedAt);

        IPage<SnCode> result = new Page<>(page, pageSize);
        snCodeMapper.selectPage(result, wrapper);
        return result.getRecords();
    }

    @Override
    public Long getSnCodeCountBySkuId(Long skuId) {
        LambdaQueryWrapper<SnCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SnCode::getSkuId, skuId);
        return snCodeMapper.selectCount(wrapper);
    }

    @Override
    public Map<String, Long> getSnCodeStatsBySkuId(Long skuId) {
        log.info("getSnCodeStatsBySkuId skuId={}", skuId);
        if (skuId == null || skuId <= 0) {
            throw new BusinessException(400, "SKU ID无效");
        }

        List<SnCode> allSns = snCodeMapper.selectList(
                new LambdaQueryWrapper<SnCode>().eq(SnCode::getSkuId, skuId));

        Map<String, Long> stats = new HashMap<>();
        stats.put("total", (long) allSns.size());
        stats.put("inStock", allSns.stream().filter(s -> s.getStatus() == 0).count());
        stats.put("locked", allSns.stream().filter(s -> s.getStatus() == 1).count());
        stats.put("sold", allSns.stream().filter(s -> s.getStatus() == 2).count());
        stats.put("delivered", allSns.stream().filter(s -> s.getStatus() == 3).count());
        stats.put("received", allSns.stream().filter(s -> s.getStatus() == 4).count());
        stats.put("completed", allSns.stream().filter(s -> s.getStatus() == 5).count());
        stats.put("voided", allSns.stream().filter(s -> s.getStatus() == 6).count());
        stats.put("returning", allSns.stream().filter(s -> s.getStatus() == 7).count());
        stats.put("returned", allSns.stream().filter(s -> s.getStatus() == 8).count());
        return stats;
    }

    @Override
    @Transactional
    public List<SnCode> generateSnCodes(Long skuId, int count) {
        log.info("generateSnCodes skuId={}, count={}", skuId, count);
        if (skuId == null || skuId <= 0) {
            throw new BusinessException(400, "SKU ID无效");
        }
        if (count <= 0 || count > 100) {
            throw new BusinessException(400, "生成数量必须在1-100之间");
        }

        Sku sku = skuMapper.selectById(skuId);
        if (sku == null) {
            throw new BusinessException("SKU不存在");
        }

        // 获取该SKU已有SN码数量，作为序号起始值
        Long existingCount = snCodeMapper.selectCount(
                new LambdaQueryWrapper<SnCode>().eq(SnCode::getSkuId, skuId));

        String prefix = sku.getSkuCode() != null ? sku.getSkuCode() : "SN";
        List<SnCode> result = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            int seq = (int) (existingCount + i);
            String snCode = prefix + "-" + String.format("%04d", seq);

            SnCode sn = new SnCode();
            sn.setSnCode(snCode);
            sn.setSkuId(skuId);
            sn.setSpuId(sku.getSpuId());
            sn.setSkuCode(sku.getSkuCode());
            sn.setSpecJson(sku.getSpecJson());
            sn.setPrice(sku.getPrice());
            sn.setStatus(0);
            sn.setSource(3); // 自动生成
            sn.setCreatedAt(LocalDateTime.now());
            sn.setUpdatedAt(LocalDateTime.now());

            // 填充SPU名称
            if (sku.getSpuId() != null) {
                Goods goods = goodsMapper.selectById(sku.getSpuId());
                if (goods != null) {
                    sn.setSpuName(goods.getName());
                }
            }

            snCodeMapper.insert(sn);
            logOperation(sn.getId(), sn.getSnCode(), skuId, "自动生成", null, 0, null, "自动生成");
            result.add(sn);
        }

        log.info("generateSnCodes 完成 skuId={}, 生成了{}个", skuId, result.size());
        return result;
    }

    @Override
    @Transactional
    public void voidSnCode(Long id, String remark) {
        log.info("voidSnCode id={}", id);
        SnCode snCode = getSnCodeById(id);
        Integer oldStatus = snCode.getStatus();

        if (oldStatus != 0 && oldStatus != 1) {
            throw new BusinessException("只能作废在库/锁定状态的SN码");
        }

        updateSnCodeStatusDirect(id, 6);
        logOperation(snCode.getId(), snCode.getSnCode(), snCode.getSkuId(),
                "作废", oldStatus, 6, null, remark);
        log.info("SN码作废成功 id={}", id);
    }

    @Override
    @Transactional
    public void applyReturnSnCode(Long id, String remark) {
        log.info("applyReturnSnCode id={}", id);
        SnCode snCode = getSnCodeById(id);
        Integer oldStatus = snCode.getStatus();

        if (oldStatus != 2 && oldStatus != 3 && oldStatus != 4) {
            throw new BusinessException("只能对已售/已发货/已签收状态发起退货");
        }

        updateSnCodeStatus(id, 7);
        logOperation(snCode.getId(), snCode.getSnCode(), snCode.getSkuId(),
                "退货申请", oldStatus, 7, null, remark);
        log.info("SN码退货申请成功 id={}", id);
    }

    @Override
    @Transactional
    public void completeReturnSnCode(Long id, String remark) {
        log.info("completeReturnSnCode id={}", id);
        SnCode snCode = getSnCodeById(id);
        Integer oldStatus = snCode.getStatus();

        if (oldStatus != 7) {
            throw new BusinessException("只能对退货中状态的SN码完成退货");
        }

        LambdaUpdateWrapper<SnCode> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SnCode::getId, id)
                .set(SnCode::getStatus, 8)
                .set(SnCode::getCurrentHolder, "已退货")
                .set(SnCode::getCurrentLocation, "已退货")
                .set(SnCode::getUpdatedAt, LocalDateTime.now());
        snCodeMapper.update(null, wrapper);

        logOperation(snCode.getId(), snCode.getSnCode(), snCode.getSkuId(),
                "退货完成", oldStatus, 8, null, remark);
        log.info("SN码退货完成，重新入库 id={}", id);
    }

    /**
     * 手动更新 SN 码状态 (仅用于异常状态修复, 正常业务请走扫码接口)
     *
     * 状态流转规则 (与前端 TRANSITION_RULES 一致):
     *   0(在库) → 1(锁定) / 6(已作废)
     *   1(锁定) → 0(在库) / 2(已售) / 6(已作废)
     *   2(已售) → 7(退货中)
     *   3(已发货) → 7(退货中)
     *   4(已签收) → 7(退货中)
     *   5(已完成) → 终态, 不可调整
     *   6(已作废) → 终态, 不可调整
     *   7(退货中) → 0(在库) / 8(已退货)
     *   8(已退货) → 0(在库) / 6(已作废)
     *
     * 注意: 正向流程(发货/签收)必须通过扫码接口, 此处不开放手动变更
     */
    private static final Map<Integer, Set<Integer>> TRANSITION_RULES = new HashMap<>();
    static {
        TRANSITION_RULES.put(0, new HashSet<>(Arrays.asList(1, 6)));
        TRANSITION_RULES.put(1, new HashSet<>(Arrays.asList(0, 2, 6)));
        TRANSITION_RULES.put(2, new HashSet<>(Arrays.asList(7)));
        TRANSITION_RULES.put(3, new HashSet<>(Arrays.asList(7)));
        TRANSITION_RULES.put(4, new HashSet<>(Arrays.asList(7)));
        TRANSITION_RULES.put(5, new HashSet<>());   // 终态
        TRANSITION_RULES.put(6, new HashSet<>());   // 终态
        TRANSITION_RULES.put(7, new HashSet<>(Arrays.asList(0, 8)));
        TRANSITION_RULES.put(8, new HashSet<>(Arrays.asList(0, 6)));
    }

    private static final Map<Integer, String> STATUS_LABELS = new HashMap<>();
    static {
        STATUS_LABELS.put(0, "在库");
        STATUS_LABELS.put(1, "锁定");
        STATUS_LABELS.put(2, "已售");
        STATUS_LABELS.put(3, "已发货");
        STATUS_LABELS.put(4, "已签收");
        STATUS_LABELS.put(5, "已完成");
        STATUS_LABELS.put(6, "已作废");
        STATUS_LABELS.put(7, "退货中");
        STATUS_LABELS.put(8, "已退货");
    }

    @Override
    @Transactional
    public void updateSnCodeStatus(Long id, Integer status) {
        log.info("updateSnCodeStatus id={}, status={}", id, status);
        SnCode snCode = getSnCodeById(id);
        Integer oldStatus = snCode.getStatus();

        // 状态范围校验
        if (status == null || status < 0 || status > 8) {
            throw new BusinessException(400, "无效的状态码: " + status);
        }
        // 终态校验
        Set<Integer> terminalSet = new HashSet<>(Arrays.asList(5, 6));
        if (terminalSet.contains(oldStatus)) {
            throw new BusinessException(400,
                "SN码当前状态为「" + STATUS_LABELS.get(oldStatus) + "」, 属于终态, 不可手动调整");
        }
        // 状态流转规则校验
        Set<Integer> allowed = TRANSITION_RULES.getOrDefault(oldStatus, new HashSet<>());
        if (!allowed.contains(status)) {
            throw new BusinessException(400,
                "不允许从「" + STATUS_LABELS.get(oldStatus) + "」直接变更为「"
                + STATUS_LABELS.get(status) + "」。"
                + "正常业务流转请使用扫码功能完成");
        }

        updateSnCodeStatusDirect(id, status);
        logOperation(snCode.getId(), snCode.getSnCode(), snCode.getSkuId(),
                "手动状态变更", oldStatus, status, null,
                "从「" + STATUS_LABELS.get(oldStatus) + "」变更为「" + STATUS_LABELS.get(status) + "」");
        log.info("SN码状态变更成功 id={}, {} → {}", id, STATUS_LABELS.get(oldStatus), STATUS_LABELS.get(status));
    }

    private void updateSnCodeStatusDirect(Long id, Integer status) {
        LambdaUpdateWrapper<SnCode> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SnCode::getId, id)
                .set(SnCode::getStatus, status)
                .set(SnCode::getUpdatedAt, LocalDateTime.now());
        snCodeMapper.update(null, wrapper);
    }

    private void logOperation(Long snCodeId, String snCode, Long skuId, String operation,
                              Integer fromStatus, Integer toStatus, Long operatorId, String remark) {
        SnCodeLog snCodeLog = new SnCodeLog();
        snCodeLog.setSnCodeId(snCodeId);
        snCodeLog.setSnCode(snCode);
        snCodeLog.setSkuId(skuId);
        snCodeLog.setOperation(operation);
        snCodeLog.setFromStatus(fromStatus);
        snCodeLog.setToStatus(toStatus);
        snCodeLog.setOperatorId(operatorId);
        snCodeLog.setRemark(remark);
        snCodeLog.setCreatedAt(LocalDateTime.now());
        snCodeLogMapper.insert(snCodeLog);
    }

    // ================== 扫码全流程实现 ==================
    // 状态机：0在库 1锁定 2已售 3已发货 4已签收 5已完成 6已作废 7退货中 8已退货

    @Override
    @Transactional
    public SnCode scanInbound(String sn, Long userId, String userName) {
        log.info("scanInbound sn={}, userId={}", sn, userId);
        SnCode snCode = getSnCodeBySn(sn);
        Integer oldStatus = snCode.getStatus();

        // 状态校验：非6/7/8 才允许入库
        if (oldStatus != null && (oldStatus == 6 || oldStatus == 7 || oldStatus == 8)) {
            throw new BusinessException("当前状态[" + oldStatus + "]不允许入库，当前SN码为：" + statusDesc(oldStatus));
        }

        // 解析真实姓名
        String realName = getUserNameById(userId);
        if (realName == null || realName.isEmpty()) {
            realName = userName;
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<SnCode> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SnCode::getId, snCode.getId())
                .set(SnCode::getStatus, 0)
                .set(SnCode::getInboundAt, now)
                .set(SnCode::getInboundUserId, userId)
                .set(SnCode::getInboundUserName, realName)
                .set(SnCode::getCurrentHolder, "仓库")
                .set(SnCode::getCurrentLocation, "仓库")
                .set(SnCode::getLogisticsNo, null)
                .set(SnCode::getUpdatedAt, now);
        snCodeMapper.update(null, wrapper);

        logOperation(snCode.getId(), snCode.getSnCode(), snCode.getSkuId(),
                "扫码入库", oldStatus, 0, userId, "扫码入库 by " + realName);

        SnCode fresh = snCodeMapper.selectById(snCode.getId());
        log.info("scanInbound 完成 sn={}, status=0", sn);
        return fresh;
    }

    @Override
    @Transactional
    public SnCode scanDeliver(String sn, String logisticsNo, Long userId, String userName) {
        log.info("scanDeliver sn={}, logisticsNo={}, userId={}", sn, logisticsNo, userId);
        SnCode snCode = getSnCodeBySn(sn);
        Integer oldStatus = snCode.getStatus();

        // 仅0/1 状态允许发货
        if (oldStatus == null || (oldStatus != 0 && oldStatus != 1)) {
            throw new BusinessException("当前状态[" + oldStatus + "]不允许发货，仅在库/锁定状态可发货");
        }
        if (!StringUtils.hasText(logisticsNo)) {
            throw new BusinessException(400, "物流单号不能为空");
        }

        String realName = getUserNameById(userId);
        if (realName == null || realName.isEmpty()) {
            realName = userName;
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<SnCode> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SnCode::getId, snCode.getId())
                .set(SnCode::getStatus, 3)
                .set(SnCode::getDeliveredAt, now)
                .set(SnCode::getLogisticsNo, logisticsNo)
                .set(SnCode::getCurrentHolder, "运输中")
                .set(SnCode::getCurrentLocation, "运输中")
                .set(SnCode::getUpdatedAt, now);
        snCodeMapper.update(null, wrapper);

        logOperation(snCode.getId(), snCode.getSnCode(), snCode.getSkuId(),
                "扫码发货", oldStatus, 3, userId, "物流单号:" + logisticsNo + " by " + realName);

        SnCode fresh = snCodeMapper.selectById(snCode.getId());
        log.info("scanDeliver 完成 sn={}, status=3", sn);
        return fresh;
    }

    @Override
    @Transactional
    public SnCode scanReceive(String sn, Long userId, String userName) {
        log.info("scanReceive sn={}, userId={}", sn, userId);
        SnCode snCode = getSnCodeBySn(sn);
        Integer oldStatus = snCode.getStatus();

        // 仅3 已发货允许签收
        if (oldStatus == null || oldStatus != 3) {
            throw new BusinessException("当前状态[" + oldStatus + "]不允许签收，仅已发货状态可签收");
        }

        String realName = getUserNameById(userId);
        if (realName == null || realName.isEmpty()) {
            realName = userName;
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<SnCode> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SnCode::getId, snCode.getId())
                .set(SnCode::getStatus, 4)
                .set(SnCode::getReceivedAt, now)
                .set(SnCode::getCurrentHolder, "客户手中")
                .set(SnCode::getCurrentLocation, "客户手中")
                .set(SnCode::getUpdatedAt, now);
        snCodeMapper.update(null, wrapper);

        logOperation(snCode.getId(), snCode.getSnCode(), snCode.getSkuId(),
                "扫码签收", oldStatus, 4, userId, "签收 by " + realName);

        SnCode fresh = snCodeMapper.selectById(snCode.getId());
        log.info("scanReceive 完成 sn={}, status=4", sn);
        return fresh;
    }

    @Override
    @Transactional
    public SnCode scanReturn(String sn, String reason) {
        log.info("scanReturn sn={}, reason={}", sn, reason);
        SnCode snCode = getSnCodeBySn(sn);
        Integer oldStatus = snCode.getStatus();

        // 仅2/3/4 状态允许退货
        if (oldStatus == null || (oldStatus != 2 && oldStatus != 3 && oldStatus != 4)) {
            throw new BusinessException("当前状态[" + oldStatus + "]不允许退货，仅已售/已发货/已签收可发起退货");
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<SnCode> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SnCode::getId, snCode.getId())
                .set(SnCode::getStatus, 7)
                .set(SnCode::getReturnAt, now)
                .set(SnCode::getCurrentHolder, "退货中")
                .set(SnCode::getCurrentLocation, "退货中")
                .set(SnCode::getRemark, reason)
                .set(SnCode::getUpdatedAt, now);
        snCodeMapper.update(null, wrapper);

        logOperation(snCode.getId(), snCode.getSnCode(), snCode.getSkuId(),
                "扫码退货", oldStatus, 7, null, "退货原因:" + reason);

        SnCode fresh = snCodeMapper.selectById(snCode.getId());
        log.info("scanReturn 完成 sn={}, status=7", sn);
        return fresh;
    }

    @Override
    @Transactional
    public SnCode scanReturnComplete(String sn, Long userId, String userName) {
        log.info("scanReturnComplete sn={}, userId={}", sn, userId);
        SnCode snCode = getSnCodeBySn(sn);
        Integer oldStatus = snCode.getStatus();

        // 仅7 退货中允许完成退货入库
        if (oldStatus == null || oldStatus != 7) {
            throw new BusinessException("当前状态[" + oldStatus + "]不允许完成退货入库，仅退货中状态可完成");
        }

        String realName = getUserNameById(userId);
        if (realName == null || realName.isEmpty()) {
            realName = userName;
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<SnCode> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SnCode::getId, snCode.getId())
                .set(SnCode::getStatus, 0)
                .set(SnCode::getInboundAt, now)
                .set(SnCode::getInboundUserId, userId)
                .set(SnCode::getInboundUserName, realName)
                .set(SnCode::getCurrentHolder, "仓库")
                .set(SnCode::getCurrentLocation, "仓库(退货入库)")
                .set(SnCode::getLogisticsNo, null)
                .set(SnCode::getUpdatedAt, now);
        snCodeMapper.update(null, wrapper);

        logOperation(snCode.getId(), snCode.getSnCode(), snCode.getSkuId(),
                "退货入库", oldStatus, 0, userId, "退货入库 by " + realName);

        SnCode fresh = snCodeMapper.selectById(snCode.getId());
        log.info("scanReturnComplete 完成 sn={}, status=0", sn);
        return fresh;
    }

    @Override
    @Transactional
    public SnCode scanVoid(String sn, String reason) {
        log.info("scanVoid sn={}, reason={}", sn, reason);
        SnCode snCode = getSnCodeBySn(sn);
        Integer oldStatus = snCode.getStatus();

        // 非2/3/4 状态才允许作废
        if (oldStatus != null && (oldStatus == 2 || oldStatus == 3 || oldStatus == 4)) {
            throw new BusinessException("当前状态[" + oldStatus + "]不允许作废，已售/已发货/已签收需走退货流程");
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<SnCode> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SnCode::getId, snCode.getId())
                .set(SnCode::getStatus, 6)
                .set(SnCode::getCurrentHolder, "已作废")
                .set(SnCode::getCurrentLocation, "已作废")
                .set(SnCode::getRemark, reason)
                .set(SnCode::getUpdatedAt, now);
        snCodeMapper.update(null, wrapper);

        logOperation(snCode.getId(), snCode.getSnCode(), snCode.getSkuId(),
                "扫码作废", oldStatus, 6, null, "作废原因:" + reason);

        SnCode fresh = snCodeMapper.selectById(snCode.getId());
        log.info("scanVoid 完成 sn={}, status=6", sn);
        return fresh;
    }

    @Override
    @Transactional
    public SnCode scanAction(String sn, String action, Map<String, Object> params) {
        log.info("scanAction sn={}, action={}", sn, action);
        if (!StringUtils.hasText(action)) {
            throw new BusinessException(400, "扫码动作不能为空");
        }
        Long userId = params != null && params.get("userId") != null ? toLong(params.get("userId")) : null;
        String userName = params != null && params.get("userName") != null ? String.valueOf(params.get("userName")) : null;

        switch (action) {
            case "inbound":
                return scanInbound(sn, userId, userName);
            case "deliver": {
                String logisticsNo = params != null && params.get("logisticsNo") != null
                        ? String.valueOf(params.get("logisticsNo")) : null;
                return scanDeliver(sn, logisticsNo, userId, userName);
            }
            case "receive":
                return scanReceive(sn, userId, userName);
            case "return": {
                String reason = params != null && params.get("reason") != null
                        ? String.valueOf(params.get("reason")) : null;
                return scanReturn(sn, reason);
            }
            case "return-complete":
                return scanReturnComplete(sn, userId, userName);
            case "void": {
                String reason = params != null && params.get("reason") != null
                        ? String.valueOf(params.get("reason")) : null;
                return scanVoid(sn, reason);
            }
            default:
                throw new BusinessException(400, "不支持的扫码动作: " + action);
        }
    }

    @Override
    @Transactional
    public Map<String, Object> batchScanInbound(List<String> sns, Long userId, String userName) {
        log.info("batchScanInbound count={}, userId={}", sns == null ? 0 : sns.size(), userId);
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> failures = new ArrayList<>();
        int success = 0;
        int failed = 0;

        if (sns != null) {
            for (String sn : sns) {
                try {
                    scanInbound(sn, userId, userName);
                    success++;
                } catch (Exception e) {
                    failed++;
                    Map<String, Object> failItem = new HashMap<>();
                    failItem.put("sn", sn);
                    failItem.put("error", e.getMessage());
                    failures.add(failItem);
                }
            }
        }

        result.put("success", success);
        result.put("failed", failed);
        result.put("failures", failures);
        result.put("total", sns == null ? 0 : sns.size());
        log.info("batchScanInbound 完成 success={}, failed={}", success, failed);
        return result;
    }

    @Override
    public String getUserNameById(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        try {
            AdminUser user = adminUserMapper.selectById(userId);
            if (user != null) {
                if (StringUtils.hasText(user.getRealName())) {
                    return user.getRealName();
                }
                if (StringUtils.hasText(user.getUsername())) {
                    return user.getUsername();
                }
            }
        } catch (Exception e) {
            log.warn("getUserNameById 异常 userId={}, err={}", userId, e.getMessage());
        }
        return null;
    }

    private String statusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "在库";
            case 1: return "锁定";
            case 2: return "已售";
            case 3: return "已发货";
            case 4: return "已签收";
            case 5: return "已完成";
            case 6: return "已作废";
            case 7: return "退货中";
            case 8: return "已退货";
            default: return "未知(" + status + ")";
        }
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
