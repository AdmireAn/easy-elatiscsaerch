package com.github.snail.selector;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.snail.config.ClientConfig;
import com.github.snail.datasource.RestConnection;
import com.github.snail.datasource.RestConnection.OpVcElasticSearchConnectionBuilder;
import com.github.snail.exception.SourceNotFoundException;
import com.google.common.collect.Lists;

/**
 * 获取es连接
 *
 * @author snail
 * Created on 2022-11-28
 */
public class SourceSelector {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourceSelector.class);


    public RestConnection select(String sourceKey) {
        OpVcElasticSearchConnectionBuilder builder = RestConnection.newBuilder();
        String readCluster = ClientConfig.getReadCluster(sourceKey);
        if (StringUtils.isBlank(readCluster)) {
            throw new SourceNotFoundException(sourceKey);
        }
        return builder.buildOne(sourceKey, readCluster);
    }

    public List<RestConnection> selects(String sourceKey) {
        OpVcElasticSearchConnectionBuilder builder = RestConnection.newBuilder();
        List<String> writeClusters = ClientConfig.getWriteCluster(sourceKey);
        if (CollectionUtils.isEmpty(writeClusters)) {
            throw new SourceNotFoundException(sourceKey);
        }
        List<RestConnection> connections = builder.build(sourceKey, writeClusters);
        List<String> names = Lists.newArrayList();
        for (RestConnection connection : connections) {
            names.add(connection.cluster().getName());
        }
        LOGGER.info("selects key is:{}, clusters:{}", sourceKey, names);
        return connections;
    }
}
