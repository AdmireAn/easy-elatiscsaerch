package com.github.snail.util;

/**
 * @author snail
 * Created on 2022-11-28
 */
public interface ThrowableSupplier<T, X extends Throwable> {

    T get() throws X;

    static <T, X extends Throwable> ThrowableSupplier<T, X> cast(ThrowableSupplier<T, X> func) {
        return func;
    }
}
