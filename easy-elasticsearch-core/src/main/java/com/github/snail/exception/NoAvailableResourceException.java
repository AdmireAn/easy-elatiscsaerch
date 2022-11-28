package com.github.snail.exception;

/**
 * @author snail
 * Created on 2022-11-28
 */
public class NoAvailableResourceException extends RuntimeException {

    private static final long serialVersionUID = -8580979490752713492L;

    public NoAvailableResourceException() {
        super();
    }

    public NoAvailableResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoAvailableResourceException(String message) {
        super(message);
    }

    public NoAvailableResourceException(Throwable cause) {
        super(cause);
    }
}
