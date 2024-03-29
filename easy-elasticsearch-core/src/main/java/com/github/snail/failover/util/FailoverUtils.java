package com.github.snail.failover.util;

import static com.github.snail.constants.StatusEnum.NO_AVAILABLE_RESOURCE;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.base.Throwables.getRootCause;
import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import com.github.snail.exception.EasyElasticsearchException;
import com.github.snail.failover.Failover;
import com.github.snail.util.ThrowableConsumer;
import com.github.snail.util.ThrowableFunction;

/**
 * @author w.vela
 */
public final class FailoverUtils {

    private FailoverUtils() {
        throw new UnsupportedOperationException();
    }

    public static <T, R, X extends Throwable> R supplyWithRetry(int maxRetryTimes,
            long sleepBetweenRetryMs, Failover<T> failover, ThrowableFunction<T, R, X> func)
            throws X {
        return supplyWithRetry(maxRetryTimes, sleepBetweenRetryMs, failover, func, alwaysTrue());
    }

    /**
     * @param failChecker {@code true} if need retry, {@code false} means no need retry and mark success
     */
    public static <T, R, X extends Throwable> R supplyWithRetry(@Nonnegative int maxRetryTimes,
            long sleepBetweenRetryMs, Failover<T> failover, ThrowableFunction<T, R, X> func,
            @Nonnull Predicate<Throwable> failChecker) throws X {
        checkArgument(maxRetryTimes > 0);
        Set<T> failed = new HashSet<>();
        Throwable lastError = null;
        for (int i = 0; i < maxRetryTimes; i++) {
            T oneAvailable = failover.getOneAvailableExclude(failed);
            if (oneAvailable != null) {
                try {
                    R result = func.apply(oneAvailable);
                    failover.success(oneAvailable);
                    return result;
                } catch (Throwable e) {
                    if (failChecker.test(e)) {
                        failover.fail(oneAvailable);
                        failed.add(oneAvailable);
                        if (sleepBetweenRetryMs > 0) {
                            sleepUninterruptibly(sleepBetweenRetryMs, MILLISECONDS);
                        }
                        lastError = e;
                        continue;
                    } else {
                        failover.success(oneAvailable);
                        throw e;
                    }
                }
            } else {
                throw new EasyElasticsearchException(NO_AVAILABLE_RESOURCE);
            }
        }
        //noinspection unchecked
        throw (X) lastError;
    }

    public static <T, R, X extends Throwable> R supply(Failover<T> failover,
            ThrowableFunction<T, R, X> func, Predicate<Throwable> failChecker) throws X {
        T oneAvailable = failover.getOneAvailable();
        if (oneAvailable != null) {
            try {
                R result = func.apply(oneAvailable);
                failover.success(oneAvailable);
                return result;
            } catch (Throwable e) {
                if (failChecker == null || failChecker.test(e)) {
                    failover.fail(oneAvailable);
                } else {
                    failover.success(oneAvailable);
                }
                throw e;
            }
        } else {
            throw new EasyElasticsearchException(NO_AVAILABLE_RESOURCE);
        }
    }

    public static <T, X extends Throwable> void runWithRetry(int maxRetryTimes,
            long sleepBetweenRetryMs, Failover<T> failover, ThrowableConsumer<T, X> func) throws X {
        supplyWithRetry(maxRetryTimes, sleepBetweenRetryMs, failover, t -> {
            func.accept(t);
            return null;
        }, alwaysTrue());
    }

    /**
     * @param failChecker {@code true} if need retry, {@code false} means no need retry and mark success
     */
    public static <T, X extends Throwable> void runWithRetry(int maxRetryTimes,
            long sleepBetweenRetryMs, Failover<T> failover, ThrowableConsumer<T, X> func,
            @Nonnull Predicate<Throwable> failChecker) throws X {
        supplyWithRetry(maxRetryTimes, sleepBetweenRetryMs, failover, t -> {
            func.accept(t);
            return null;
        }, failChecker);
    }

    public static <T, X extends Throwable> void run(Failover<T> failover,
            ThrowableConsumer<T, X> func, Predicate<Throwable> failChecker) throws X {
        supply(failover, t -> {
            func.accept(t);
            return null;
        }, failChecker);
    }

    public static boolean isHostUnavailable(Throwable t) {
        Throwable rootCause = getRootCause(t);
        if (rootCause instanceof NoRouteToHostException) {
            return true;
        }
        if (rootCause instanceof UnknownHostException) {
            return false;
        }
        if (rootCause instanceof MalformedURLException) {
            return false;
        }
        if (rootCause instanceof ConnectException) {
            if (rootCause.getMessage() != null
                    && rootCause.getMessage().toLowerCase().contains("connection refused")) {
                return true;
            }
        }
        if (rootCause instanceof SocketTimeoutException) {
            if (rootCause.getMessage() != null
                    && rootCause.getMessage().toLowerCase().contains("connect timed out")) {
                return true;
            }
        }
        return false;
    }
}
