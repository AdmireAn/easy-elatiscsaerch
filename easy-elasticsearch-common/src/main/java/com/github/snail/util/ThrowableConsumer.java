package com.github.snail.util;

import static java.util.Objects.requireNonNull;

/**
 * @author snail
 * Created on 2022-11-28
 */
@FunctionalInterface
public interface ThrowableConsumer<T, X extends Throwable> {

    void accept(T t) throws X;

    default ThrowableConsumer<T, X> andThen(ThrowableConsumer<? super T, X> after) {
        requireNonNull(after);
        return t -> {
            accept(t);
            after.accept(t);
        };
    }
}
