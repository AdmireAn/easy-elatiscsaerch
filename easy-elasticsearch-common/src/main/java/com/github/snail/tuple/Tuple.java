package com.github.snail.tuple;

/**
 * @author snail
 * Created on 2022-11-28
 */
public final class Tuple {
    private Tuple() {
        throw new UnsupportedOperationException();
    }

    public static <A, B> TwoTuple<A, B> tuple(final A a, final B b) {
        return new TwoTuple<>(a, b);
    }

    public static <A, B, C> ThreeTuple<A, B, C> tuple(final A a, final B b, final C c) {
        return new ThreeTuple<>(a, b, c);
    }
}
