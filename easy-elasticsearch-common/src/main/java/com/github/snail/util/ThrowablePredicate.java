package com.github.snail.util;

/**
 * @author snail
 * Created on 2022-11-28
 */
@FunctionalInterface
public interface ThrowablePredicate<T, X extends Throwable> {

    boolean test(T t) throws X;
}
