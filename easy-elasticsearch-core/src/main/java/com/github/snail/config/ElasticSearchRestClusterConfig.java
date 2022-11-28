package com.github.snail.config;

import com.github.snail.enums.ElasticSearchVersion;

/**
 * @author snail
 * Created on 2022-11-28
 */
public interface ElasticSearchRestClusterConfig {

    String getName();

    ElasticSearchVersion getVersion();
}
