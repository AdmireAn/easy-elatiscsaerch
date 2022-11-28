package com.github.snail.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author snail
 * Created on 2022-11-28
 */
public interface ElasticSearchIndexPartitioner {
    /**
     * 获取分区后的索引名称
     *
     * @param shardFactor 分区factor
     * @return 真实索引名称
     */
    String partition(@Nonnull String index, @Nullable Object shardFactor);
}
