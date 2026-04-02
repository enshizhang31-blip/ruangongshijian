package com.salemanager.modules.sn.param;

import lombok.Data;

/**
 * SN码请求参数
 */
@Data
public class SnCodeParam {

    private String sn;
    private String[] sns;
    private Long goodsId;
    private Long skuId;
    private String remark;
}
