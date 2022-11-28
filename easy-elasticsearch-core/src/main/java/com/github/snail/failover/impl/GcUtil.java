package com.github.snail.failover.impl;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;


/**
 * @author huangli
 * Created on 2019-12-30
 */
final class GcUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(GcUtil.class);

    private static final ConcurrentHashMap<Reference<Object>, Runnable> REF_MAP = new ConcurrentHashMap<>();
    private static final ReferenceQueue<Object> REF_QUEUE = new ReferenceQueue<>();

    @VisibleForTesting
    static ConcurrentHashMap<Reference<Object>, Runnable> getRefMap() {
        return REF_MAP;
    }

    public static void register(Object resource, Runnable cleaner) {
        if (resource != null && cleaner != null) {
            PhantomReference<Object> ref = new PhantomReference<>(resource, REF_QUEUE);
            REF_MAP.put(ref, cleaner);
        }
    }

    public static void doClean() {
        Reference<?> ref = REF_QUEUE.poll();
        while (ref != null) {
            Runnable cleaner = REF_MAP.remove(ref);
            try {
                cleaner.run();
            } catch (Throwable t) {
                LOGGER.warn("Failover GC doClean failed", t);
            }
            ref = REF_QUEUE.poll();
        }
    }
}
