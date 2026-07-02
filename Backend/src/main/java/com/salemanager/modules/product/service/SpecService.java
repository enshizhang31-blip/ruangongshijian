package com.salemanager.modules.product.service;

import com.salemanager.modules.product.model.SpecName;
import com.salemanager.modules.product.model.SpecValue;
import com.salemanager.modules.product.param.SpecParam;
import com.salemanager.modules.product.param.SpecValueParam;

import java.util.List;

/**
 * 规格服务接口
 */
public interface SpecService {

    /**
     * 获取规格列表
     */
    List<SpecName> getSpecList();

    /**
     * 新增规格
     */
    void createSpec(SpecParam param);

    /**
     * 更新规格
     */
    void updateSpec(Long id, SpecParam param);

    /**
     * 删除规格
     */
    void deleteSpec(Long id);

    /**
     * 获取规格值
     */
    List<SpecValue> getSpecValues(Long specId);

    /**
     * 新增规格值
     */
    void createSpecValue(Long specId, SpecValueParam param);

    /**
     * 批量添加规格值 (供"添加值"窗口批量录入使用)
     * @param specId 规格ID
     * @param values 值列表, 例如 ["黑色", "白色", "红色"]
     * @return 新创建的规格值列表
     */
    List<SpecValue> batchCreateSpecValues(Long specId, List<String> values);

    /**
     * 更新规格值
     */
    void updateSpecValue(Long id, SpecValueParam param);

    /**
     * 删除规格值
     */
    void deleteSpecValue(Long id);

    /**
     * 批量解析规格ID+值ID为可读名称
     */
    List<java.util.Map<String, Object>> resolveSpecItems(List<java.util.Map<String, Long>> items);
}