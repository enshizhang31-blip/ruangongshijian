package com.salemanager.modules.product.enums;

/**
 * 商品状态枚举
 */
public enum GoodsStatusEnum {

    DRAFT(0, "草稿"),
    ON_SALE(1, "上架"),
    OFF_SALE(2, "下架"),
    DELETED(3, "已删除");

    private final Integer code;
    private final String description;

    GoodsStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static GoodsStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (GoodsStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
