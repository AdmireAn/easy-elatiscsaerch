package com.github.snail.config;

import java.util.Collections;
import java.util.List;

import com.github.snail.exception.SourceNotFoundException;

/**
 * @author snail
 * Created on 2022-11-28
 * 资源，索引，集群名的映射关系.
 */
public class ClientConfig {

    private static final int DEFAULT_TIMEOUT = 5000;

    public static String type(String sourceKey) {
        return "easyWriteElasticsearch";
    }

    public static int getPartitionCount(String sourceKey) {
        return 2;
    }

    /**
     * 获取写索引名
     * 写索引目前仍然使用真实名
     * 如果配置中包含了writeAlias，则使用writeAlias
     */
    public static String writeIndexName(String sourceKey) throws SourceNotFoundException {
        return "easy-write-elasticsearch-index";
    }

    public static String readIndexName(String sourceKey) throws SourceNotFoundException {
        return "easy-write-elasticsearch-index-alias";
    }

    public static String getReadCluster(String sourceKey) {
        return "localCluster";
    }

    public static List<String> getWriteCluster(String sourceKey) {
        return Collections.singletonList("localCluster");
    }

    public static int syncWriteTimeout(String sourceKey) {
        return DEFAULT_TIMEOUT;
    }
}
