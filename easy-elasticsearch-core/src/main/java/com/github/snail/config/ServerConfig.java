package com.github.snail.config;

import static com.github.snail.constant.ElasticSearchConstant.Connect.DEFAULT_CONNECT_TIMEOUT;
import static com.github.snail.constant.ElasticSearchConstant.Connect.DEFAULT_MAX_RETRY_TIMEOUT;
import static com.github.snail.constant.ElasticSearchConstant.Connect.DEFAULT_REST_CLIENT_SIZE;
import static com.github.snail.constant.ElasticSearchConstant.Connect.DEFAULT_SOCKET_TIMEOUT;
import static com.github.snail.constant.ElasticSearchConstant.Connect.DEFAULT_THREAD_SIZE;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * TODO 这里可以接入连接ES server的各种参数
 * 比如：连接数、连接超时时间、io线程数量等
 *
 * 根据个人需要可以用zk替换
 * @author snail
 * Created on 2022-11-28
 */
public class ServerConfig {

    //TODO 这里是测试的临时配置，生产使用时请替换为线上配置文件
    private static Map<String, ServerConfigModel> genTmpConf() {
        ServerConfigModel model = new ServerConfigModel();
        model.setConnectionSize(5);
        model.setCluster("localCluster");
        model.setHostAndPorts(Collections.singletonList("127.0.0.1:9200"));
        model.setConfigMaxRetryTimeout(50000);
        model.setConnectionTimeoutMills(50000);
        model.setIoThreadCount(500);
        model.setSocketTimeoutMills(50000);

        Map<String, ServerConfigModel> tmpConf = Maps.newHashMap();
        tmpConf.put(model.getCluster(), model);
        return tmpConf;
    }

    public static int connectionSize(String cluster) {
        int connectionSize = genTmpConf().get(cluster).getConnectionSize();
        return connectionSize > 0 ? connectionSize : DEFAULT_REST_CLIENT_SIZE;
    }

    public static List<String> getHostAndPorts(String cluster) {
        return genTmpConf().get(cluster).getHostAndPorts();
    }

    public static int getSocketTimeoutMills(String cluster) {
        int socketTimeoutMills = genTmpConf().get(cluster).getSocketTimeoutMills();
        return socketTimeoutMills > 0 ? socketTimeoutMills : DEFAULT_SOCKET_TIMEOUT;
    }

    public static int getIoThreadCount(String cluster) {
        int ioThreadCount = genTmpConf().get(cluster).getIoThreadCount();
        return ioThreadCount > 0 ? ioThreadCount : DEFAULT_THREAD_SIZE;
    }

    public static int getConnectionTimeoutMills(String cluster) {
        int connectionTimeoutMills = genTmpConf().get(cluster).getConnectionTimeoutMills();
        return connectionTimeoutMills > 0 ? connectionTimeoutMills : DEFAULT_CONNECT_TIMEOUT;
    }

    public static int getMaxRetryTimeout(String cluster) {
        int maxRetryTimeout = genTmpConf().get(cluster).getConfigMaxRetryTimeout();
        return maxRetryTimeout > 0 ? maxRetryTimeout : DEFAULT_MAX_RETRY_TIMEOUT;
    }
}
