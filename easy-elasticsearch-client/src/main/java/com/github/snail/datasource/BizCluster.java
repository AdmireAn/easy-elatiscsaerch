package com.github.snail.datasource;

import java.util.Map;

import javax.annotation.Nullable;

import com.github.snail.config.ElasticSearchRestClusterConfig;
import com.github.snail.enums.ElasticSearchVersion;
import com.google.common.collect.Maps;

/**
 *
 * @author snail
 * Created on 2022-11-28
 */
public enum BizCluster implements ElasticSearchRestClusterConfig {

    //新的测试环境,SRE统一维护
    LOCAL_CLUSTER("localCluster", ElasticSearchVersion.VERSION_6_X);

    private static final Map<String, BizCluster> CLUSTER_MAPPER =
            Maps.newConcurrentMap();

    static {
        for (BizCluster cluster : values()) {
            CLUSTER_MAPPER.put(cluster.name, cluster);
        }
    }

    private final String name;
    private final ElasticSearchVersion version;

    BizCluster(String name, ElasticSearchVersion version) {
        this.name = name;
        this.version = version;
    }

    @Nullable
    public static BizCluster getCluster(String name) {
        return CLUSTER_MAPPER.get(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ElasticSearchVersion getVersion() {
        return version;
    }
}
