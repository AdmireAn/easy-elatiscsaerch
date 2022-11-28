package com.github.snail.client;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.snail.az.AvailableZone;
import com.github.snail.az.PhysicalAvailableZone;
import com.github.snail.config.ElasticSearchRestClusterConfig;
import com.github.snail.tuple.ThreeTuple;
import com.github.snail.tuple.Tuple;
import com.github.snail.tuple.TwoTuple;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * @author snail
 * Created on 2022-11-28
 */
public final class ElasticSearchRestClientHolder {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ElasticSearchRestClientHolder.class);

    private static final ConcurrentHashMap<ThreeTuple<AvailableZone, ? extends ElasticSearchRestClusterConfig,
            PhysicalAvailableZone>,
            ElasticSearchRestClientHolder>
            INSTANCES //
            = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<TwoTuple<AvailableZone, String>, ElasticSearchRestClientHolder>
            DEPRECATED_INSTANCES //
            = new ConcurrentHashMap<>();

    private final Supplier<ElasticSearchRestClientWithCheck> newNode;
    private final ElasticSearchRestClusterConfig clusterConfig;
    private final String bizName;
    private AvailableZone az;
    private PhysicalAvailableZone paz;

    private static final double BACK_OFF_EXP = 1.5;
    private static final long MAX_BACK_OFF_MS = TimeUnit.MINUTES.toMillis(5);
    private final long initBackOffMs = ThreadLocalRandom.current().nextInt(1000, 2000);
    private static final ScheduledExecutorService DELAY_CLEANUP_SCHEDULED_EXECUTOR_SERVICE =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                    .setNameFormat("delay-cleanup-schedule-%d").build());
    private static final long WAIT_AFTER_CLOSE = SECONDS.toMillis(10);


    private ElasticSearchRestClientHolder(AvailableZone az, ElasticSearchRestClusterConfig clusterConfig,
            PhysicalAvailableZone paz) {

        bizName = clusterConfig.getName();
        this.clusterConfig = clusterConfig;
        this.az = az;
        this.paz = paz;
        this.newNode = createRestClient();
    }

    private Supplier<ElasticSearchRestClientWithCheck> createRestClient() {
        return () -> new ElasticSearchRestClientWithCheck(bizName);
    }


    public static ElasticSearchRestClientHolder of(AvailableZone az, ElasticSearchRestClusterConfig clusterConfig,
            PhysicalAvailableZone paz) {
        ThreeTuple<AvailableZone, ElasticSearchRestClusterConfig, PhysicalAvailableZone> tuple =
                Tuple.tuple(az, clusterConfig, paz);
        ElasticSearchRestClientHolder restClientHolder = INSTANCES.get(tuple);
        if (restClientHolder != null) {
            return restClientHolder;
        }

        return INSTANCES.computeIfAbsent(tuple, t -> {
            LOGGER.info("elasticsearch rest client created. az: {} bizName: {}", az,
                    clusterConfig.getName());
            return new ElasticSearchRestClientHolder(t.getFirst(), clusterConfig, paz);
        });
    }

    public ElasticsearchRestClient getOneClient() {
        ElasticSearchRestClientWithCheck clientWithCheck = getElasticSearchRestClientWithCheck();
        return clientWithCheck.getOne();
    }

    private ElasticSearchRestClientWithCheck getElasticSearchRestClientWithCheck() {
        ElasticSearchRestClientWithCheck clientWithCheck = newNode.get();
        if (clientWithCheck == null) {
            throw new IllegalStateException("no config found for " + az + " " + clusterConfig.getName());
        }
        return clientWithCheck;
    }
}
