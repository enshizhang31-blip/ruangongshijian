package com.salemanager.modules.customer.enums;

/**
 * 客户状态枚举
 */
public enum CustomerStatusEnum {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用"),
    DELETED(2, "已删除");

    private final Integer code;
    private final String description;

    CustomerStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static CustomerStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CustomerStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
