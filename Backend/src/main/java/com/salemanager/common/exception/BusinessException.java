package com.salemanager.common.exception;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {

    private Integer code = 500;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}