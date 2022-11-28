package com.github.snail.datasource;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.snail.config.ElasticSearchRestClusterConfig;
import com.github.snail.ElasticSearchRestIndexConfig;
import com.github.snail.config.ClientConfig;
import com.github.snail.util.ElasticSearchIndexPartitioner;
import com.github.snail.util.PartitionUtils;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

/**
 * ES几个概念
 * 集群(cluster):集群中有多个节点
 * 分片(shard):类似MYSQL分表的概念。代表索引分片，es可以把一个完整的索引分成多个分片，这样的好处是可以把一个大的索引拆分成多个，分布到不同的节点上
 * 分区:类似MYSQL的分库的概念。多分区即多索引
 * 副本(replica):索引副本，完全拷贝shard的内容，shard与replica的关系可以是一对多，同一个shard可以有一个或多个replica
 * 索引(index): 索引，具有相同结构的文档集合，类似于关系型数据库的数据库实例（6.0.0版本type废弃后，索引的概念下降到等同于数据库表的级别）
 * type：类型，原本是在索引(Index)内进行的逻辑细分，但后来发现企业研发为了增强可阅读性和可维护性，制订的规范约束，同一个索引下很少还会再使用type进行逻辑拆分（如同一个索引下既有订单数据，又有评论数据），因而在6.0
 * .0版本之后，此定义废弃。
 * <p>
 * 做了分片的索引 写入数据需指定具体的索引名字  读索引使用别名即可
 * <p>
 * 比如分区2 op-vc-item-index-0,op-vc-item-index-1
 * 写入数据需要写入哪一个分区
 * 读数据使用别名op-vc-item-index-alias即可
 * <p>
 * resource+cluster唯一确定了一个AladdinRestConnection实例
 *
 * @author snail
 * Created on 2022-11-28
 */
public class RestConnection implements ElasticSearchRestIndexConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestConnection.class);

    private static final String MAGIC = "ewe-";


    /**
     * 资源名
     */
    private final String key;

    private final ElasticSearchRestClusterConfig cluster;

    /**
     * 写别名
     */
    private final String writeAliasName;

    /**
     * 读别名
     */
    private final String readAliasName;

    /**
     * cluster由外部提供，用与构建读。
     */
    RestConnection(String key, String cluster) {
        this.key = key;
        this.readAliasName = ClientConfig.readIndexName(key);
        this.writeAliasName = ClientConfig.writeIndexName(key);
        this.cluster = BizCluster.getCluster(cluster);
        LOGGER.info("key:{}, readAliasName:{}, writeAliasName:{}, clusterName:{}",
                key, readAliasName, writeAliasName, this.cluster == null
                                                    ? cluster + " but cluster is null" : cluster);

    }

    @Nonnull
    public ElasticSearchRestClusterConfig cluster() {
        return cluster;
    }

    /**
     * 框架底层用该名字做写操作，会使用该方法和shardFactor直接拼接
     */
    @Nonnull
    @Override
    public String indexName() {
        return writeAliasName;
    }

    /**
     * 框架底层用该名字做读操作
     */
    @Nullable
    @Override
    public String aliasName() {
        return readAliasName;
    }

    public ElasticSearchIndexPartitioner partitioner() {
        return PartitionUtils.partitioner();
    }

    public String key() {
        return this.key;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public static OpVcElasticSearchConnectionBuilder newBuilder() {
        return new OpVcElasticSearchConnectionBuilder();
    }

    /**
     * 构建1个或多个链接
     */
    public static class OpVcElasticSearchConnectionBuilder {

        public RestConnection buildOne(String key, String cluster) {
            return new RestConnection(key, cluster);
        }

        public List<RestConnection> build(String key, List<String> clusters) {
            if (clusters.size() <= 1) {
                return ImmutableList
                        .of(new RestConnection(key, clusters.get(0)));
            }
            return clusters.stream()
                    .map(cn -> new RestConnection(key, cn))
                    .collect(Collectors.toList());
        }
    }

    /**
     * kconf key + cluster唯一确定一个connection实例
     * kuaishou-es用ConcurrentMap缓存connection
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RestConnection that = (RestConnection) o;
        return Objects.equal(key, that.key)
                && Objects.equal(cluster, that.cluster);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key, cluster);
    }
}
