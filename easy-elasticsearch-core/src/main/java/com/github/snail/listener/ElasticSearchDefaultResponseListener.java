package com.github.snail.listener;

import javax.annotation.Nullable;

import org.apache.commons.lang3.time.StopWatch;
import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.snail.ElasticSearchExecuteContext;
import com.github.snail.config.ElasticSearchRestClusterConfig;
import com.github.snail.ElasticSearchRestIndexConfig;
import com.github.snail.client.ElasticsearchRestClient;

/**
 * @author snail
 * Created on 2022-11-28
 */
public final class ElasticSearchDefaultResponseListener implements ElasticSearchResponseListener {
    private static final Logger logger = LoggerFactory
            .getLogger(ElasticSearchDefaultResponseListener.class);

    @Override
    public void onSuccess(StopWatch stopWatch, ElasticSearchRestIndexConfig config, ElasticsearchRestClient restClient,
            Response response, String index, String type, String opType) {
        onSuccess(stopWatch, config.cluster(), restClient, response, index, type, opType);
    }

    @Override
    public void onFailure(ElasticSearchRestIndexConfig config, ElasticsearchRestClient restClient, Exception exception,
            String index, String type, String opType) {
        onFailure(config.cluster(), restClient, exception, index, type, opType);
    }

    @Override
    public void onSuccess(ElasticSearchExecuteContext context) {
        onSuccess(context.getStopWatch(), context.getConfig().cluster(), context.getRestClient(), context.getResponse(),
                context.getIndex(), context.getType(), context.getOpType());
    }

    @Override
    public void onFailure(ElasticSearchExecuteContext context) {
        onFailure(context.getConfig().cluster(), context.getRestClient(), context.getException(), context.getIndex(),
                context.getType(), context.getOpType());
    }

    private void onSuccess(@Nullable StopWatch stopWatch, ElasticSearchRestClusterConfig clusterConfig,
            ElasticsearchRestClient restClient, Response response, String index, String type,
            String opType) {
        restClient.getFailover().success(restClient);
        // log and perf here
    }

    private void onFailure(ElasticSearchRestClusterConfig cluster, ElasticsearchRestClient restClient,
            Exception exception, String index, String type, String opType) {
        restClient.getFailover().fail(restClient);
        logger.error("{} {} {} es failure.", cluster.getName(), index, opType, exception);
        // log and perf here
    }
}
