package com.github.snail.listener;

import org.apache.commons.lang3.time.StopWatch;
import org.elasticsearch.client.Response;

import com.github.snail.ElasticSearchExecuteContext;
import com.github.snail.ElasticSearchRestIndexConfig;
import com.github.snail.client.ElasticsearchRestClient;

/**
 * @author snail
 * Created on 2022-11-28
 */
public interface ElasticSearchResponseListener {
    void onSuccess(StopWatch stopWatch, ElasticSearchRestIndexConfig config, ElasticsearchRestClient restClient,
            Response response, String index, String type, String opType);

    void onFailure(ElasticSearchRestIndexConfig config, ElasticsearchRestClient restClient, Exception exception,
            String index, String type, String opType);

    default void onSuccess(ElasticSearchExecuteContext context) {
        onSuccess(context.getStopWatch(), context.getConfig(), context.getRestClient(),
                context.getResponse(), context.getIndex(), context.getType(), context.getOpType());
    }

    default void onFailure(ElasticSearchExecuteContext context) {
        onFailure(context.getConfig(), context.getRestClient(), context.getException(), context.getIndex(),
                context.getType(), context.getOpType());
    }
}
