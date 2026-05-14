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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public List<SnCode> getSnCodeList(String keyword, Integer status, Integer page, Integer pageSize) {
        log.info("getSnCodeList keyword={}, status={}, page={}, pageSize={}", keyword, status, page, pageSize);

        LambdaQueryWrapper<SnCode> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(SnCode::getSnCode, keyword)
                    .or()
                    .like(SnCode::getSpuName, keyword));
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
                    .like(SnCode::getSpuName, keyword));
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
        stats.put("sold", allSns.stream().filter(s -> s.getStatus() == 1).count());
        stats.put("voided", allSns.stream().filter(s -> s.getStatus() == 2).count());
        stats.put("returning", allSns.stream().filter(s -> s.getStatus() == 3).count());
        stats.put("returned", allSns.stream().filter(s -> s.getStatus() == 4).count());
        return stats;
    }

    @Override
    @Transactional
    public void voidSnCode(Long id, String remark) {
        log.info("voidSnCode id={}", id);
        SnCode snCode = getSnCodeById(id);
        Integer oldStatus = snCode.getStatus();

        if (oldStatus != 0) {
            throw new BusinessException("只能作废在库状态的SN码");
        }

        updateSnCodeStatus(id, 2);
        logOperation(snCode.getId(), snCode.getSnCode(), snCode.getSkuId(),
                "作废", oldStatus, 2, null, remark);
        log.info("SN码作废成功 id={}", id);
    }

    @Override
    @Transactional
    public void applyReturnSnCode(Long id, String remark) {
        log.info("applyReturnSnCode id={}", id);
        SnCode snCode = getSnCodeById(id);
        Integer oldStatus = snCode.getStatus();

        if (oldStatus != 1) {
            throw new BusinessException("只能对已售状态的SN码发起退货");
        }

        updateSnCodeStatus(id, 3);
        logOperation(snCode.getId(), snCode.getSnCode(), snCode.getSkuId(),
                "退货申请", oldStatus, 3, null, remark);
        log.info("SN码退货申请成功 id={}", id);
    }

    @Override
    @Transactional
    public void completeReturnSnCode(Long id, String remark) {
        log.info("completeReturnSnCode id={}", id);
        SnCode snCode = getSnCodeById(id);
        Integer oldStatus = snCode.getStatus();

        if (oldStatus != 3) {
            throw new BusinessException("只能对退货中状态的SN码完成退货");
        }

        updateSnCodeStatus(id, 4);
        logOperation(snCode.getId(), snCode.getSnCode(), snCode.getSkuId(),
                "退货完成", oldStatus, 4, null, remark);
        log.info("SN码退货完成，重新入库 id={}", id);
    }

    private void updateSnCodeStatus(Long id, Integer status) {
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
}
