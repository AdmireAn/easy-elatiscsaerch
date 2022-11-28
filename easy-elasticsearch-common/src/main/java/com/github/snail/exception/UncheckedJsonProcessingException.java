package com.github.snail.exception;

import java.io.UncheckedIOException;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author snail
 * Created on 2022-11-28
 */
public class UncheckedJsonProcessingException extends UncheckedIOException {

    public UncheckedJsonProcessingException(JsonProcessingException cause) {
        super(cause.getMessage(), cause);
    }
}
