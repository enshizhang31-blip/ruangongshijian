package com.salemanager.modules.customer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.salemanager.modules.customer.model.CartItem;
import com.salemanager.common.exception.BusinessException;

import java.util.List;
import java.util.Map;

public interface AppCartService extends IService<CartItem> {

    /**
     * 加入购物车。已存在则累加 quantity。
     */
    CartItem addItem(Long customerId, Long spuId, Long skuId, Integer quantity);

    /**
     * 查询当前客户购物车的详情（含 SKU/SPU/价格/库存）。
     */
    List<Map<String, Object>> listDetail(Long customerId);

    /**
     * 修改数量 / 选中态。
     */
    void updateByCustomer(Long customerId, Long id, Integer quantity, Integer selected);

    /**
     * 删除一条。
     */
    void removeByCustomer(Long customerId, Long id);

    /**
     * 清空（保留预留给将来，目前未暴露接口）。
     */
    void clearByCustomer(Long customerId);

    /**
     * 工具方法：校验购物车条目所有权。
     */
    CartItem requireOwned(Long customerId, Long id);

    /**
     * 工具方法：业务异常。
     */
    static BusinessException error(String message) {
        return new BusinessException(message);
    }
}
