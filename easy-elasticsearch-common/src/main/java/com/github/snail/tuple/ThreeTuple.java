package com.github.snail.tuple;

/**
 * @author snail
 * Created on 2022-11-28
 */
public class ThreeTuple<A, B, C> extends TwoTuple<A, B> {

    private final C third;

    /**
     * use {@link Tuple#tuple(Object, Object, Object)} instead
     */
    @Deprecated
    public ThreeTuple(final A a, final B b, final C c) {
        super(a, b);
        third = c;
    }

    public C getThird() {
        return third;
    }

    @Override
    public String toString() {
        return "(" + getFirst() + ", " + getSecond() + ", " + third + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (third == null ? 0 : third.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ThreeTuple<?, ?, ?> other = (ThreeTuple<?, ?, ?>) obj;
        if (third == null) {
            if (other.third != null) {
                return false;
            }
        } else if (!third.equals(other.third)) {
            return false;
        }
        return true;
    }

}
