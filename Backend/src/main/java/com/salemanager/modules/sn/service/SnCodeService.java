package com.salemanager.modules.sn.service;

import com.salemanager.common.exception.BusinessException;
import com.salemanager.modules.sn.model.SnCode;
import com.salemanager.modules.sn.model.SnCodeLog;
import com.salemanager.modules.sn.param.SnCodeParam;

import java.util.List;
import java.util.Map;

/**
 * SN码服务接口
 */
public interface SnCodeService {

    /**
     * 获取SN码列表
     */
    List<SnCode> getSnCodeList(String keyword, Integer status, Integer page, Integer pageSize);

    /**
     * 获取SN码总数
     */
    Long getSnCodeCount(String keyword, Integer status);

    /**
     * 获取SN码详情
     *
     * @param id SN码ID
     * @return SN码信息
     * @throws BusinessException SN码不存在时抛出异常
     */
    SnCode getSnCodeById(Long id);

    /**
     * 按SN码查询
     *
     * @param sn SN码
     * @return SN码信息
     * @throws BusinessException SN码不存在时抛出异常
     */
    SnCode getSnCodeBySn(String sn);

    /**
     * 录入SN码
     */
    void createSnCode(SnCodeParam param);

    /**
     * 批量录入SN码
     */
    Map<String, Object> batchCreateSnCode(SnCodeParam param);

    /**
     * 绑定SN码到订单
     */
    void bindSnCode(String sn, Long orderId);

    /**
     * 解绑SN码
     */
    void unbindSnCode(Long id);

    /**
     * 获取SN码操作日志
     */
    List<SnCodeLog> getSnCodeLogs(Long snId, Integer page, Integer pageSize);

    /**
     * 获取SN码日志总数
     */
    Long getSnCodeLogCount(Long snId);

    /**
     * 按商品ID获取SN码列表
     */
    List<SnCode> getSnCodesByGoodsId(Long goodsId);
}
