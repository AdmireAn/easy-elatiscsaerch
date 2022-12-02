package com.github.snail.exception;

import com.github.snail.constants.StatusEnum;

/**
 * @author snail
 * Created on 2022-11-28
 * 异常父类，所有异常情况通过异常返回
 */
public class EasyElasticsearchException extends RuntimeException {

    private String message;

    private int code;

    public EasyElasticsearchException(String message, int code) {
        this.message = message;
        this.code = code;
    }

    public EasyElasticsearchException(StatusEnum statusEnum) {
        this.message = statusEnum.getMessage();
        this.code = statusEnum.getCode();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
