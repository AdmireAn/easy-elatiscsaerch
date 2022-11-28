package com.github.snail;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.http.client.CredentialsProvider;

import com.github.snail.config.ElasticSearchRestClusterConfig;
import com.github.snail.util.ElasticSearchIndexPartitioner;

/**
 * @author snail
 * Created on 2022-11-28
 */
public interface ElasticSearchRestIndexConfig {


    @Nonnull
    ElasticSearchRestClusterConfig cluster();

    @Nonnull
    String indexName();

    @Nullable
    String aliasName();

    default ElasticSearchIndexPartitioner partitioner() {
        return (s, t) -> indexName();
    }

    @Nullable
    default CredentialsProvider credentialsProvider() {
        return null;
    }

    default ElasticSearchRestIndex get() {
        return ElasticSearchRestIndex.of(this);
    }
}