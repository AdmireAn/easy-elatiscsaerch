package com.github.snail.exception;


import static com.github.snail.constants.StatusEnum.NOT_FOUND;

/**
 * @author snail
 * Created on 2022-11-28
 * 索引未接入
 */
public class SourceNotFoundException extends EasyElasticsearchException {

    public SourceNotFoundException(String message) {
        super(String.format(NOT_FOUND.getMessage(), message), NOT_FOUND.getCode());
    }
}
