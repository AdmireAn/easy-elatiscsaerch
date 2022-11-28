package com.github.snail;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

import com.google.common.annotations.Beta;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.UncheckedTimeoutException;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * @author snail
 * Created on 2022-11-28
 */
public final class ElasticSearchRestFuture<V> extends AbstractFuture<V> {

    private static final long DEFAULT_TIMEOUT_SECONDS = 60;

    /**
     * Creates a new {@code SettableFuture} that can be completed or cancelled by a later method call.
     */
    public static <V> ElasticSearchRestFuture<V> create() {
        return new ElasticSearchRestFuture<>();
    }

    @CanIgnoreReturnValue
    @Override
    public boolean set(@Nullable V value) {
        return super.set(value);
    }

    @CanIgnoreReturnValue
    @Override
    public boolean setException(Throwable throwable) {
        return super.setException(throwable);
    }

    @Beta
    @CanIgnoreReturnValue
    @Override
    public boolean setFuture(ListenableFuture<? extends V> future) {
        return super.setFuture(future);
    }

    @CanIgnoreReturnValue
    @Override
    public V get() throws InterruptedException, ExecutionException {
        try {
            return get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new UncheckedTimeoutException(e);
        }
    }

    @CanIgnoreReturnValue
    @Override
    public V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return super.get(timeout, unit);
    }

    private ElasticSearchRestFuture() {
    }
}