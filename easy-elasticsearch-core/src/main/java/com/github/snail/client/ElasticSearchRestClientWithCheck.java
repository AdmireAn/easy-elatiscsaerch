package com.github.snail.client;

import static com.github.snail.util.EasyElasticsearchUtils.check;
import static com.github.snail.util.EasyElasticsearchUtils.closeQuietly;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.snail.config.ServerConfig;
import com.github.snail.exception.NoAvailableResourceException;
import com.github.snail.failover.SimpleFailover;
import com.github.snail.failover.impl.PriorityFailover;
import com.github.snail.failover.impl.RatioWeightFunction;
import com.github.snail.failover.impl.WeightListener;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.HostAndPort;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * desc: 代理ES RestClient,添加定时检测可用性功能
 *
 * @author snail
 * Created on 2022-11-28
 */
class ElasticSearchRestClientWithCheck implements Closeable {

    private static final Logger logger = LoggerFactory
            .getLogger(ElasticSearchRestClientWithCheck.class);
    private static final int MAX_LOOP_TIMES = 5;
    private static final int MONITOR_SECONDS = 15;
    private static final int HEART_BEAT_SECONDS = 30;
    private static final double RECOVERED_INIT_RATE = 0.2;
    private static final double FAILED_REDUCE_RATE = 0.01;
    private static final double SUCCESS_INCR_RATE = 0.1;
    private static final int FAIL_CHECK_DURATION = 3000;
    private static final int MIN_CONNECTION_POOL_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(3);
    private static final long HEARTBEAT_TIMEOUT_MILLS = TimeUnit.SECONDS.toMillis(90);
    private static final int CHECKER_CONNECTION_TIME_OUT = (int) TimeUnit.SECONDS.toMillis(5);

    private final String innerBizName;
    private final AtomicLong lastHeartbeatTimeMills;
    private final RateLimiter refreshRateLimiter = RateLimiter.create(1.0);
    private final AtomicReference<SimpleFailover<ElasticsearchRestClient>> restClientFailoverReference =
            new AtomicReference<>();
    private final AtomicReference<ScheduledExecutorService> monitorExecutorServiceReference;
    private final ScheduledExecutorService heartbeatExecutorService;

    ElasticSearchRestClientWithCheck(String bizName) {

        Preconditions.checkArgument(StringUtils.isNotEmpty(bizName));

        innerBizName = bizName;

        refresh();
        lastHeartbeatTimeMills = new AtomicLong(System.currentTimeMillis());
        monitorExecutorServiceReference = new AtomicReference<>(createMonitorExecutorService());
        heartbeatExecutorService = Executors.newScheduledThreadPool(1, new ThreadFactoryBuilder()
                .setNameFormat("es-heartbeat-" + bizName + "-%d").setDaemon(true).build());

        // http调用hang住,直接重建
        heartbeatExecutorService.scheduleAtFixedRate(() -> {
            try {
                if (System.currentTimeMillis()
                        - lastHeartbeatTimeMills.get() > HEARTBEAT_TIMEOUT_MILLS) {
                    logger.warn("{} heartbeat timeout, refresh.", bizName);
                    refresh();
                    // 重建monitorService
                    ExecutorService old = monitorExecutorServiceReference
                            .getAndSet(createMonitorExecutorService());
                    old.shutdownNow();
                }
            } catch (Throwable t) {
                logger.error("es heartbeat error. biz: {}", bizName, t);
            }
        }, 3, HEART_BEAT_SECONDS, TimeUnit.SECONDS);
    }

    ElasticsearchRestClient getOne() {
        // 集群下线后，没有可用节点，restClientFailoverReference.get() 可能为null
        ElasticsearchRestClient client = null;
        SimpleFailover<ElasticsearchRestClient> failover = restClientFailoverReference.get();
        if (failover != null) {
            client = failover.getOneAvailable();
        }
        if (client == null) {
            refresh();
            throw new NoAvailableResourceException(
                    "no " + innerBizName + " es rest client. will rebuild");
        }
        return client;
    }

    public synchronized void close() {
        // 强制shutdown即可
        monitorExecutorServiceReference.get().shutdownNow();
        heartbeatExecutorService.shutdownNow();
        restClientFailoverReference.get().getAll().forEach(e -> closeQuietly(e.getRestClient()));
    }

    private ScheduledExecutorService createMonitorExecutorService() {

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2,
                new ThreadFactoryBuilder().setNameFormat("es-monitor-" + innerBizName + "-%d")
                        .setDaemon(true).build());

        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                SimpleFailover<ElasticsearchRestClient> failover = restClientFailoverReference.get();
                if (failover == null) {
                    refresh();
                    return;
                }
                failover.getAll().forEach(e -> {
                    if (!check(e.getHostAndPort().getHost(), e.getHostAndPort().getPort(),
                            CHECKER_CONNECTION_TIME_OUT)) {
                        logger.warn("es base check failed. biz: {} {}", innerBizName,
                                e.getHostAndPort());
                        failover.down(e);
                    }
                });
            } catch (Throwable t) {
                logger.error("es index check failed. biz: {}", innerBizName, t);
            }
        }, 3, 3, TimeUnit.SECONDS);

        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                int statusCode = getOne().getRestClient()
                        .performRequest("GET", "/", ImmutableMap.of()).getStatusLine()
                        .getStatusCode();
                logger.debug("es monitor. biz: {} status: {}", innerBizName, statusCode);
                lastHeartbeatTimeMills.set(System.currentTimeMillis());
            } catch (IllegalStateException e) {
                String message = e.getMessage();
                // 不可恢复异常，直接重建
                if (StringUtils.contains(message, "I/O reactor status: STOPPED")) {
                    logger.warn("es restclient rebuild. biz: {}", innerBizName);
                    refresh();
                }
            } catch (Throwable t) {
                logger.warn("es monitor failed. {}", t.getClass().getSimpleName());
            }
        }, 3, MONITOR_SECONDS, TimeUnit.SECONDS);

        return scheduledExecutorService;
    }

    private synchronized void refresh() {

        if (!refreshRateLimiter.tryAcquire()) {
            logger.warn("es refresh exceed threshold. ignore. bizName: {}", innerBizName);
            return;
        }

        List<String> servers = ServerConfig.getHostAndPorts(innerBizName);
        int clientSize = ServerConfig.connectionSize(innerBizName);

        // fail fast
        if (CollectionUtils.isEmpty(servers)) {
            throw new IllegalArgumentException(
                    "es config error. no servers. config: " + servers);
        }

        Set<HostAndPort> selectedHosts = new HashSet<>();
        int curLoop = 0;
        int maxLoop = Math.max(MAX_LOOP_TIMES, clientSize * 2);
        do {
            String server = servers.get(ThreadLocalRandom.current().nextInt(servers.size()));
            HostAndPort hostAndPort = HostAndPort
                    .fromString(StringUtils.removeStart(server, "http://"));
            if (!selectedHosts.contains(hostAndPort) //
                    && check(hostAndPort)) {
                selectedHosts.add(hostAndPort);
            }
            curLoop++;
        } while (curLoop < maxLoop && //
                (selectedHosts.size() < clientSize));

        if (selectedHosts.size() == 0) {
            logger.warn("no available host. ignore refresh. bizName: {}", innerBizName);
            return;
        }

        CopyOnWriteArraySet<HostAndPort> currentHostAndPorts = new CopyOnWriteArraySet<>(selectedHosts);

        Set<ElasticsearchRestClient> hosts = currentHostAndPorts.stream()
                .map(t -> new ElasticsearchRestClient(
                        buildRestClient(innerBizName, "http://" + t.toString()), t))
                .collect(Collectors.toSet());

        SimpleFailover<ElasticsearchRestClient> old = restClientFailoverReference
                .getAndSet(buildFailover(innerBizName, hosts));

        restClientFailoverReference.get().getAll()
                .forEach(f -> f.setFailover(restClientFailoverReference.get()));

        if (old != null) {
            old.getAll().forEach(e -> closeQuietly(e.getRestClient()));
        }

        logger.info("es config refreshed. biz: {} hosts: {}", innerBizName, hosts.stream()
                .map(ElasticsearchRestClient::getHostAndPort).collect(Collectors.toList()));
    }

    @SuppressWarnings({"Unchecked"})
    private SimpleFailover<ElasticsearchRestClient> buildFailover(String bizName,
            Collection<ElasticsearchRestClient> clients) {
        return PriorityFailover.<ElasticsearchRestClient> newBuilder() //
                .checker(server -> pingServer(server))
                .checkDuration(Duration.ofSeconds(60))
                .addResources(clients, /*minWeight*/100.0)
                .weightFunction(new RatioWeightFunction(/*failKeepRateOfCurrentWeight*/0.5,
                        /*successIncreaseRateOfMaxWeight*/0.01, /*recoverThreshold*/2, /*downThreshold*/
                        0.1)) //默认情况下，权重变成0算作不健康，健康检查成功1次就立刻恢复权重。这个行为可以通过WeightFunction定制
                .concurrencyControl(
                        true) //并发度控制，并发度高的资源，流量会减少。 并发度初始值是0，getOneAvailable会将并发度加1，success/fail/down会将并发度减1
                // ，内部选择资源的时候，当前权重会除以（并发度+1），也就是说如果并发度为1，有效权重就会减半（除以2），并发度是2时，有效权重就会变为1/3。
                .enableAutoPriority(5) // 自动优先级管理 第一组5个，剩下的归为第二组
                .weightListener(new WeightListener() {
                                    @Override
                                    public void onSuccess(double maxWeight, double minWeight, int priority,
                                            double currentOldWeight,
                                            double currentNewWeight, Object resource) {
                                        logger.info("ElasticsearchRestClient onSuccess,{}.{},{},{}", maxWeight,
                                                minWeight, priority,
                                                currentNewWeight);
                                    }

                                    @Override
                                    public void onFail(double maxWeight, double minWeight, int priority,
                                            double currentOldWeight,
                                            double currentNewWeight, Object resource) {
                                        logger.info("ElasticsearchRestClient onFail,{}.{},{},{}", maxWeight,
                                                minWeight, priority,
                                                currentNewWeight);
                                    }
                                }
                ).name("elasticsearch-" + bizName) //
                .build();
    }

    private boolean pingServer(ElasticsearchRestClient server) {
        return check(server.getHostAndPort().getHost(), server.getHostAndPort().getPort(),
                CHECKER_CONNECTION_TIME_OUT);
    }

    private RestClient buildRestClient(String bizName, String host) {

        int connectTimeout = ServerConfig.getConnectionTimeoutMills(bizName);
        int socketTimeout = ServerConfig.getSocketTimeoutMills(bizName);
        int threadSize = ServerConfig.getIoThreadCount(bizName);
        int maxRetryTimeout = ServerConfig.getMaxRetryTimeout(bizName);

        final int configConnectTimeout = connectTimeout;
        final int configSocketTimeout = socketTimeout;
        final int configThreadSize = threadSize;
        final int configMaxRetryTimeout = maxRetryTimeout;

        return RestClient.builder(HttpHost.create(host))
                .setRequestConfigCallback(builder -> builder.setConnectTimeout(configConnectTimeout)
                        .setSocketTimeout(configSocketTimeout).setConnectionRequestTimeout(
                                Math.max(configSocketTimeout / 3, MIN_CONNECTION_POOL_TIMEOUT)))
                .setHttpClientConfigCallback(builder -> {
                    IOReactorConfig reactorConfig = IOReactorConfig.custom()
                            .setConnectTimeout(configConnectTimeout)
                            .setSoTimeout(configSocketTimeout)
                            .setIoThreadCount(configThreadSize)
                            .build();
                    ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true)
                            .setNameFormat("elasticsearch-rest-" + bizName + "-" + host + "-%d")
                            .build();
                    return builder.setDefaultIOReactorConfig(reactorConfig) //
                            .setThreadFactory(threadFactory);
                }) //
                .setMaxRetryTimeoutMillis(configMaxRetryTimeout)
                .setFailureListener(new RestClient.FailureListener() {

                    @Override
                    public void onFailure(Node node) {
                        logger.warn("es host failed. biz: {} host: {}", bizName, host);
                    }
                }).build();
    }
}
