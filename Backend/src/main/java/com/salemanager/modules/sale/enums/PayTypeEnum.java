package com.salemanager.modules.sale.enums;

/**
 * 支付方式枚举
 */
public enum PayTypeEnum {

    WECHAT(1, "微信支付"),
    ALIPAY(2, "支付宝"),
    BALANCE(3, "余额支付"),
    CASH(4, "现金");

    private final Integer code;
    private final String description;

    PayTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static PayTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PayTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
