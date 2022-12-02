package com.github.snail;

import static com.github.snail.constant.ElasticSearchConstant.DOC;
import static com.github.snail.constant.ElasticSearchConstant.DOC_AS_UPSERT;
import static com.github.snail.constant.ElasticSearchConstant.Method.GET;
import static com.github.snail.constant.ElasticSearchConstant.Method.POST;
import static com.github.snail.constant.ElasticSearchConstant.VERSION;
import static com.github.snail.enums.ElasticSearchVersion.VERSION_2_X;
import static com.github.snail.util.JsonUtils.toJSON;
import static com.github.snail.util.QueryBuilderUtils.buildQueryJSON;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.index.query.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.snail.ElasticSearchExecuteContext.Builder;
import com.github.snail.az.AvailableZone;
import com.github.snail.az.PhysicalAvailableZone;
import com.github.snail.client.ElasticSearchRestClientHolder;
import com.github.snail.client.ElasticsearchRestClient;
import com.github.snail.listener.ElasticSearchDefaultResponseListener;
import com.github.snail.listener.ElasticSearchResponseListener;
import com.github.snail.tuple.ThreeTuple;
import com.github.snail.tuple.Tuple;
import com.github.snail.tuple.TwoTuple;
import com.github.snail.util.ElasticSearchIndexPartitioner;
import com.github.snail.util.ObjectMapperUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * @author snail
 * Created on 2022-11-28
 */
public final class ElasticSearchRestIndex {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchRestIndex.class);
    private String indexName = "";
    private String aliasName = "";
    private final ElasticSearchRestIndexConfig restIndexConfig;
    private final ElasticSearchResponseListener responseListener;
    private final ElasticSearchIndexPartitioner elasticSearchIndexPartitioner;
    private final AvailableZone az;
    private final PhysicalAvailableZone paz;

    private static final ConcurrentMap<TwoTuple<AvailableZone, ElasticSearchRestIndexConfig>,
            ElasticSearchRestIndex> INSTANCES = new ConcurrentHashMap<>();


    static <T extends ElasticSearchRestIndexConfig> ElasticSearchRestIndex of(@Nonnull T conf) {
        //TODO 默认AZ
        return of(conf, AvailableZone.of("local"), PhysicalAvailableZone.of("local"));
    }

    static <T extends ElasticSearchRestIndexConfig> ElasticSearchRestIndex of(@Nonnull T conf, AvailableZone az,
            PhysicalAvailableZone paz) {
        checkNotNull(conf);
        return INSTANCES
                .computeIfAbsent(Tuple.tuple(az, conf, paz), tuple -> new ElasticSearchRestIndex(tuple.getSecond(),
                        tuple.getFirst(), paz));
    }

    private ElasticSearchRestIndex(ElasticSearchRestIndexConfig conf, AvailableZone az, PhysicalAvailableZone paz) {
        checkArgument(conf != null);
        checkArgument(StringUtils.isNotEmpty(conf.indexName()));
        this.restIndexConfig = conf;
        this.indexName = conf.indexName();
        this.aliasName = conf.aliasName();
        this.elasticSearchIndexPartitioner = conf.partitioner();
        this.responseListener = new ElasticSearchDefaultResponseListener();
        this.az = az;
        this.paz = paz;
    }

    /**
     * 允许shardFactor参数是空的，如果有值则拼接到索引名后面，如果没有值则不做拼接直接用原名
     * 使用场景：阿拉丁中同时存在不需要索引拆分的业务和需要拆分的业务，因此需要适配这两种业务场景的更新
     */
    public ListenableFuture<Response> updateBulkWithShardNullable(String type,
            Collection<ThreeTuple<Object, String, Object>> sources, boolean upsert) {
        List<InnerElasticSearchUpdateParam> tuples = sources.stream()
                .map(t -> {
                    String idxName = this.indexName;
                    if (t.getSecond() != null) {
                        idxName = shardIndexName(t.getFirst());
                    }
                    return new InnerElasticSearchUpdateParam(idxName, type, t.getSecond(), null, null, t.getThird());
                }).collect(toList());
        return innerUpdateBulk(tuples, upsert);
    }

    private String shardIndexName(Object obj) {
        return elasticSearchIndexPartitioner.partition(indexName, obj);
    }

    public ListenableFuture<Response> innerUpdateBulk(Collection<InnerElasticSearchUpdateParam> sources,
            boolean upsert) {
        return innerUpdateBulk(sources, upsert, emptyMap());
    }

    public ListenableFuture<Response> innerUpdateBulk(Collection<InnerElasticSearchUpdateParam> sources, boolean upsert,
            Map<String, String> paramMap) {
        if (isEmpty(sources)) {
            return Futures.immediateFuture(null);
        }
        String endpoint = "/_bulk";

        // 构造请求参数
        List<String> ret = new ArrayList<>();
        sources.forEach((t) -> {
            Map<String, Object> data = newHashMap();
            data.put("_index", t.index);
            data.put("_type", t.type);
            data.put("_id", t.id);
            if (StringUtils.isNotEmpty(t.parent)) {
                data.put("_parent", t.parent);
            }
            if (StringUtils.isNotEmpty(t.routing)) {
                data.put("_routing", t.routing);
            }
            if (t.version != null) {
                data.put(VERSION, t.version);
            }
            if (t.retryOnConflict > 0) {
                String retryOnConflictProperty = (restIndexConfig.cluster()
                        .getVersion() == VERSION_2_X) ? "_retry_on_conflict" : "retry_on_conflict";
                data.put(retryOnConflictProperty, t.retryOnConflict);
            }
            Map<String, Map<String, Object>> result = newHashMap();
            result.put("update", data);
            ret.add(toJSON(result));
            if (t.includeNull) {
                // 序列化时包含空值
                ret.add(toJSON(ImmutableMap.of(DOC, t.source, DOC_AS_UPSERT, upsert)));
            } else {
                // 序列化时不包含空值
                ret.add(toJSON(ImmutableMap.of(DOC, t.source, DOC_AS_UPSERT, upsert)));
            }
        });

        String type = sources.stream().map(e -> e.type).findFirst().orElse("unknown");
        String source = Joiner.on('\n').skipNulls().join(ret) + "\n";
        HttpEntity entity = buildRequestEntity(source);

        LOGGER.debug("updateBulk. dsl: {}", source);
        return asyncExecute0(POST, endpoint, paramMap, entity, type, "insert");
    }

    public static HttpEntity buildRequestEntity(String data) {
        return new NStringEntity(data, ContentType.APPLICATION_JSON);
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private ElasticSearchRestFuture<Response> asyncExecute0(String methodName, String endpoint,
            Map<String, String> paramMap, HttpEntity entity, String opType, String type) {
        ElasticSearchRestFuture<Response> future = ElasticSearchRestFuture.create();
        ElasticsearchRestClient restClient = restClient();

        Request request = new Request(methodName, endpoint);
        request.setEntity(entity);
        if (paramMap != null) {
            paramMap.forEach(request::addParameter);
        }

        restClient.getRestClient().performRequestAsync(request, new ResponseListener() {
            private final Builder builder = ElasticSearchExecuteContext.newBuilder()
                    .stopWatch(new StopWatch())
                    .restClient(restClient)
                    .config(restIndexConfig)
                    .index(indexName)
                    .type(type)
                    .startNanos(System.nanoTime())
                    .opType(opType);

            @Override
            public void onSuccess(Response response) {
                LOGGER.error("onSuccess");
                future.set(response);
                responseListener.onSuccess(builder.response(response).build());
            }

            @Override
            public void onFailure(Exception exception) {
                future.setException(exception);
                responseListener.onFailure(builder.exception(exception).build());
                LOGGER.error("onFailure");
            }
        });
        return future;
    }

    public static class InnerElasticSearchUpdateParam {
        private final String index;
        private final String type;
        private final String id;
        private final String routing;
        private final String parent;
        private final Object source;
        private int retryOnConflict = 0;
        // update 扩展参数，支持任意 update rest中的请求参数
        private final Map<String, Object> extraMap;
        // 支持自定义endpoint,默认格式为/index/type/id/_update, 自定义可以直接写比如/employees/_doc/_update_by_query
        private final String endpoint;
        // 序列化 RequestBody时，是否包含 null 值
        private final boolean includeNull;
        // 版本控制，update操作支持指定version, 如果当前修改数据时的版本号和ES中的版本号相同，则可以修改成功，否则修改失败
        private final Long version;

        public InnerElasticSearchUpdateParam(String index, String type, String id, String routing,
                String parent, Object source) {
            this(index, type, id, routing, parent, source, ImmutableMap.of());
        }

        public InnerElasticSearchUpdateParam(String index, String type, String id, String routing,
                String parent, Object source, Map<String, Object> extraMap) {
            this(index, type, id, routing, parent, source, ImmutableMap.of(), "");
        }

        @SuppressWarnings("checkstyle:ParameterNumber")
        public InnerElasticSearchUpdateParam(String index, String type, String id, String routing,
                String parent, Object source, Map<String, Object> extraMap, String endpoint) {
            this(index, type, id, routing, parent, source, ImmutableMap.of(), "", true);
        }

        @SuppressWarnings("checkstyle:ParameterNumber")
        public InnerElasticSearchUpdateParam(String index, String type, String id, String routing,
                String parent, Object source, Map<String, Object> extraMap, String endpoint, boolean includeNull) {
            this(index, type, id, routing, parent, source, ImmutableMap.of(), endpoint, includeNull, null);
        }

        @SuppressWarnings("checkstyle:ParameterNumber")
        public InnerElasticSearchUpdateParam(String index, String type, String id, String routing, String parent,
                Object source, Map<String, Object> extraMap, String endpoint, boolean includeNull, Long version) {
            checkArgument(index != null);
            checkArgument(type != null);
            checkArgument(id != null);
            checkArgument(endpoint != null);

            this.index = index;
            this.type = type;
            this.id = id;
            this.routing = routing;
            this.parent = parent;
            this.source = source;
            this.extraMap = extraMap;
            this.endpoint = endpoint;
            this.includeNull = includeNull;
            this.version = version;
        }
    }


    private ElasticsearchRestClient restClient() {
        return ElasticSearchRestClientHolder.of(az, restIndexConfig.cluster(), paz).getOneClient();
    }


    public Response search(ElasticSearchRestQuery query) {
        return innerSearch(aliasName, query);
    }


    private Response innerSearch(String index, ElasticSearchRestQuery query) {
        checkNotNull(query.getType());
        checkNotNull(query.getIndices());
        Map<String, Object> params = new HashMap<>();

        String strIndices = Joiner.on(',').skipNulls().join(query.getIndices());
        if (StringUtils.isEmpty(strIndices)) {
            strIndices = index;
        }
        String endpoint = Joiner.on('/').join("", strIndices, query.getType(), "_search");
        if (StringUtils.isNotEmpty(query.getRouting())) {
            endpoint += "?routing=" + query.getRouting();
        }
        QueryBuilder queryBuilder = query.getQueryBuilder();
        if (queryBuilder != null) {
            String queryJson = buildQueryJSON(queryBuilder);
            params.put("query", ObjectMapperUtils.fromJSON(queryJson, Object.class));
        } else {
            String queryJson = query.getQueryJson();
            params.put("query", ObjectMapperUtils.fromJSON(queryJson, Object.class));
        }
        if (StringUtils.isNotEmpty(query.getHighLightJson())) {
            params.put("highlight",
                    ObjectMapperUtils.fromJSON(query.getHighLightJson(), Object.class));
        }
        if (StringUtils.isNotEmpty(query.getAggrJson())) {
            params.put("aggs", ObjectMapperUtils.fromJSON(query.getAggrJson(), Object.class));
        }
        if (StringUtils.isNotEmpty(query.getPostFilterJson())) {
            params.put("post_filter",
                    ObjectMapperUtils.fromJSON(query.getPostFilterJson(), Object.class));
        }
        if (isNotEmpty(query.getSorts())) {
            params.put("sort", query.getSorts());
        }
        if (isNotEmpty(query.getSourceResult())) {
            params.put("_source", ImmutableMap.of("includes", query.getSourceResult()));
        }
        if (isNotEmpty(query.getIncludeFields())) {
            params.put("_source", query.getIncludeFields());
        }
        params.put("from", query.getFrom());
        params.put("size", query.getSize());
        params.putAll(query.getExtraParams());

        String source = toJSON(params);

        LOGGER.debug("search. dsl: {}", source);
        return executeRequest(endpoint, query, source, index);
    }

    private Response executeRequest(String endpoint, ElasticSearchRestQuery query, String source, String index) {
        ElasticsearchRestClient restClient = restClient();
        HttpEntity entity = buildRequestEntity(source);

        Builder builder = ElasticSearchExecuteContext.newBuilder()
                .stopWatch(new StopWatch())
                .restClient(restClient)
                .config(restIndexConfig)
                .index(index)
                .startNanos(System.nanoTime())
                .opType("search");

        try {
            Request request = new Request(GET, endpoint);
            Optional.ofNullable(query.getRequestParams()).ifPresent(params ->
                    params.forEach(request::addParameter));
            request.setEntity(entity);
            Response resp = restClient.getRestClient().performRequest(request);
            responseListener.onSuccess(builder.response(resp).build());
            return resp;
        } catch (ResponseException e) {
            return e.getResponse();
        } catch (IOException e) {
            responseListener.onFailure(builder.exception(e).build());
            return null;
        }
    }

}
