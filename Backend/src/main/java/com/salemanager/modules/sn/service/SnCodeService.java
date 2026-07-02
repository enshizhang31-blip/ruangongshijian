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

    /**
     * 按SKU ID获取SN码列表
     */
    List<SnCode> getSnCodesBySkuId(Long skuId, Integer page, Integer pageSize);

    /**
     * 按SKU ID获取SN码总数
     */
    Long getSnCodeCountBySkuId(Long skuId);

    /**
     * 获取SKU的SN码状态统计（在库/已售/作废/退货中/已退货）
     */
    Map<String, Long> getSnCodeStatsBySkuId(Long skuId);

    /**
     * 作废SN码（状态→2）
     */
    void voidSnCode(Long id, String remark);

    /**
     * 退货申请（状态 1→3）
     */
    void applyReturnSnCode(Long id, String remark);

    /**
     * 退货完成（状态 3→4，退回在库）
     */
    void completeReturnSnCode(Long id, String remark);

    /**
     * 为指定SKU自动生成SN码
     *
     * @param skuId SKU ID
     * @param count 生成数量
     * @return 生成的SN码列表
     */
    List<SnCode> generateSnCodes(Long skuId, int count);

    /**
     * 更新SN码状态
     */
    void updateSnCodeStatus(Long id, Integer status);

    // ====================== 扫码全流程接口 ======================

    /**
     * 扫码入库（状态机：任意非已作废/退货中/已退货 → 0在库）
     */
    SnCode scanInbound(String sn, Long userId, String userName);

    /**
     * 扫码发货（状态机：0在库/1锁定 → 3已发货）
     */
    SnCode scanDeliver(String sn, String logisticsNo, Long userId, String userName);

    /**
     * 扫码签收（状态机：3已发货 → 4已签收）
     */
    SnCode scanReceive(String sn, Long userId, String userName);

    /**
     * 扫码退货（状态机：2已售/3已发货/4已签收 → 7退货中）
     */
    SnCode scanReturn(String sn, String reason);

    /**
     * 扫码退货完成（状态机：7退货中 → 0在库，重新入库）
     */
    SnCode scanReturnComplete(String sn, Long userId, String userName);

    /**
     * 扫码作废（状态机：非已售/已发货/已签收 → 6已作废）
     */
    SnCode scanVoid(String sn, String reason);

    /**
     * 通用扫码动作分发
     */
    SnCode scanAction(String sn, String action, Map<String, Object> params);

    /**
     * 批量扫码入库
     */
    Map<String, Object> batchScanInbound(List<String> sns, Long userId, String userName);

    /**
     * 根据用户ID获取用户姓名
     */
    String getUserNameById(Long userId);
}
