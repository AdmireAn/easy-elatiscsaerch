package com.github.snail;

import org.apache.commons.lang3.time.StopWatch;
import org.elasticsearch.client.Response;

import com.github.snail.client.ElasticsearchRestClient;

/**
 * @author snail
 * Created on 2022-11-28
 */
public class ElasticSearchExecuteContext {
    private final StopWatch stopWatch;
    private final ElasticSearchRestIndexConfig config;
    private final ElasticsearchRestClient restClient;
    private final Response response;
    private final String index;
    private final String type;
    private final String opType;
    private final long startNanos;
    private final Exception exception;

    private ElasticSearchExecuteContext(Builder builder) {
        stopWatch = builder.stopWatch;
        config = builder.config;
        restClient = builder.restClient;
        response = builder.response;
        index = builder.index;
        type = builder.type;
        opType = builder.opType;
        startNanos = builder.startNanos;
        exception = builder.exception;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public StopWatch getStopWatch() {
        return stopWatch;
    }

    public ElasticSearchRestIndexConfig getConfig() {
        return config;
    }

    public ElasticsearchRestClient getRestClient() {
        return restClient;
    }

    public Response getResponse() {
        return response;
    }

    public String getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public String getOpType() {
        return opType;
    }

    public long getStartNanos() {
        return startNanos;
    }

    public Exception getException() {
        return exception;
    }

    public static final class Builder {
        private StopWatch stopWatch;
        private ElasticSearchRestIndexConfig config;
        private ElasticsearchRestClient restClient;
        private Response response;
        private String index;
        private String type;
        private String opType;
        private long startNanos;
        private Exception exception;

        private Builder() {
        }

        public Builder stopWatch(StopWatch val) {
            stopWatch = val;
            return this;
        }

        public Builder config(ElasticSearchRestIndexConfig val) {
            config = val;
            return this;
        }

        public Builder restClient(ElasticsearchRestClient val) {
            restClient = val;
            return this;
        }

        public Builder response(Response val) {
            response = val;
            return this;
        }

        public Builder index(String val) {
            index = val;
            return this;
        }

        public Builder type(String val) {
            type = val;
            return this;
        }

        public Builder opType(String val) {
            opType = val;
            return this;
        }

        public Builder startNanos(long val) {
            startNanos = val;
            return this;
        }

        public Builder exception(Exception val) {
            exception = val;
            return this;
        }

        public ElasticSearchExecuteContext build() {
            return new ElasticSearchExecuteContext(this);
        }
    }
}
