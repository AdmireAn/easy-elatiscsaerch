package com.github.snail.util;

import org.elasticsearch.common.Strings;

/**
 * @author snail
 * Created on 2022-11-28
 * 索引分区方案工具类
 */
public class PartitionUtils {


    private static final String SPLITTER = "-";

    /**
     * 索引分区器
     * indexName-shard
     */
    public static ElasticSearchIndexPartitioner partitioner() {
        return (index, shardFactor) -> shardFactor == null || Strings.isNullOrEmpty(String.valueOf(shardFactor))
                                       ? index
                                       : index + SPLITTER + shardFactor;
    }
}
