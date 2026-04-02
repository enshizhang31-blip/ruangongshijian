package com.salemanager.modules.sn.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        LambdaQueryWrapper<SnCode> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                    .like(SnCode::getSnCode, keyword)
                    .or()
                    .like(SnCode::getSpuName, keyword));
        }

        if (status != null) {
            wrapper.eq(SnCode::getStatus, status);
        }

        wrapper.orderByDesc(SnCode::getCreatedAt);

        int offset = (page - 1) * pageSize;
        wrapper.last("LIMIT " + offset + ", " + pageSize);

        return snCodeMapper.selectList(wrapper);
    }

    @Override
    public Long getSnCodeCount(String keyword, Integer status) {
        LambdaQueryWrapper<SnCode> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
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
        return snCodeMapper.selectById(id);
    }

    @Override
    public SnCode getSnCodeBySn(String sn) {
        LambdaQueryWrapper<SnCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SnCode::getSnCode, sn);
        return snCodeMapper.selectOne(wrapper);
    }

    @Override
    public void createSnCode(SnCodeParam param) {
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

        // 记录日志
        logOperation(snCode.getId(), snCode.getSnCode(), param.getSkuId(), "录入", null, 0, null, param.getRemark());
    }

    @Override
    @Transactional
    public Map<String, Object> batchCreateSnCode(SnCodeParam param) {
        int success = 0;
        int failed = 0;
        List<String> errors = new ArrayList<>();

        if (param.getSns() != null) {
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
        return result;
    }

    @Override
    public void bindSnCode(String sn, Long orderId) {
        SnCode snCode = getSnCodeBySn(sn);
        if (snCode == null) {
            return;
        }

        Integer oldStatus = snCode.getStatus();
        snCode.setStatus(1); // 已售
        snCode.setSoldAt(LocalDateTime.now());
        snCode.setUpdatedAt(LocalDateTime.now());

        LambdaUpdateWrapper<SnCode> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SnCode::getId, snCode.getId())
                .set(SnCode::getStatus, 1)
                .set(SnCode::getSoldAt, LocalDateTime.now())
                .set(SnCode::getUpdatedAt, LocalDateTime.now());
        snCodeMapper.update(null, wrapper);

        // 记录日志
        logOperation(snCode.getId(), snCode.getSnCode(), snCode.getSkuId(), "销售", oldStatus, 1, null, "订单ID: " + orderId);
    }

    @Override
    public void unbindSnCode(Long id) {
        SnCode snCode = snCodeMapper.selectById(id);
        if (snCode == null) {
            return;
        }

        Integer oldStatus = snCode.getStatus();
        snCode.setStatus(0); // 回到在库
        snCode.setUpdatedAt(LocalDateTime.now());

        LambdaUpdateWrapper<SnCode> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SnCode::getId, id)
                .set(SnCode::getStatus, 0)
                .set(SnCode::getUpdatedAt, LocalDateTime.now());
        snCodeMapper.update(null, wrapper);

        // 记录日志
        logOperation(snCode.getId(), snCode.getSnCode(), snCode.getSkuId(), "解绑", oldStatus, 0, null, null);
    }

    @Override
    public List<SnCodeLog> getSnCodeLogs(Long snId, Integer page, Integer pageSize) {
        LambdaQueryWrapper<SnCodeLog> wrapper = new LambdaQueryWrapper<>();

        if (snId != null) {
            wrapper.eq(SnCodeLog::getSnCodeId, snId);
        }

        wrapper.orderByDesc(SnCodeLog::getCreatedAt);

        int offset = (page - 1) * pageSize;
        wrapper.last("LIMIT " + offset + ", " + pageSize);

        return snCodeLogMapper.selectList(wrapper);
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
        LambdaQueryWrapper<SnCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SnCode::getSpuId, goodsId);
        wrapper.orderByDesc(SnCode::getCreatedAt);
        return snCodeMapper.selectList(wrapper);
    }

    private void logOperation(Long snCodeId, String snCode, Long skuId, String operation,
                              Integer fromStatus, Integer toStatus, Long operatorId, String remark) {
        SnCodeLog log = new SnCodeLog();
        log.setSnCodeId(snCodeId);
        log.setSnCode(snCode);
        log.setSkuId(skuId);
        log.setOperation(operation);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperatorId(operatorId);
        log.setRemark(remark);
        log.setCreatedAt(LocalDateTime.now());
        snCodeLogMapper.insert(log);
    }
}
