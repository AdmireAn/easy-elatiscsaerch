package com.github.snail.service.impl;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.snail.ElasticSearchRestIndex;
import com.github.snail.ElasticSearchRestQuery;
import com.github.snail.config.ClientConfig;
import com.github.snail.constant.SourceKey;
import com.github.snail.datasource.RestConnection;
import com.github.snail.exception.SourceNotFoundException;
import com.github.snail.index.Document;
import com.github.snail.result.ESBulkResponse;
import com.github.snail.result.ESSearchResponse;
import com.github.snail.result.SearchResult;
import com.github.snail.selector.SourceSelector;
import com.github.snail.service.ESSearchService;
import com.github.snail.tuple.ThreeTuple;
import com.github.snail.tuple.Tuple;
import com.github.snail.util.ModelTransferUtils;
import com.github.snail.util.ObjectMapperUtils;
import com.github.snail.util.SingletonFactory;
import com.google.common.collect.Lists;

/**
 * @author snail
 * Created on 2022-11-28
 */
public class ESSearchServiceImpl implements ESSearchService {
    private static final Logger log = LoggerFactory.getLogger(ESSearchServiceImpl.class);

    private static final String LOG_TEMPLATE = "search context sourceName:{} aliasName:{} shards:{}"
            + " statue:{} exe_cost:{}, es_cost:{}, total:{}, from:{}, size:{}, dsl:\n{}";

    private static final String LOG_SCROLL_TEMPLATE = "scroll context sourceName:{} indexName:{} "
            + "type:{} exe_cost:{}, es_cost:{}, total:{}, from:{}, size:{}, dsl:\n{}";

    private final SourceSelector sourceSelector = SingletonFactory.getInstance(SourceSelector.class);

    @Override
    public <T> SearchResult<T> read(ElasticSearchRestQuery query, SourceKey sourceKey) {
        return read(query, sourceKey.key());
    }

    @Override
    public <T> void bulkUpsert(List<T> docs) {
        List<ThreeTuple<Object, String, Object>> dataList = buildBulkData(docs);
        if (CollectionUtils.isEmpty(dataList)) {
            log.warn("ESSearchService bulkUpsert empty docs");
            return;
        }
        Document document = ModelTransferUtils.format(docs.get(0));
        String sourceKey = document.getSourceKey();

        List<RestConnection> connections = sourceSelector.selects(sourceKey);

        connections.forEach(connection -> {
            try {
                ElasticSearchRestIndex elasticSearchRestIndex = connection.get();
                log.info("asyncBulkUpsert operation, resource:{}, index:{}, type:{}, size:{}, request:{}",
                        sourceKey,
                        connection.indexName(),
                        ClientConfig.type(sourceKey),
                        dataList.size(),
                        dataList);
                Response response = elasticSearchRestIndex
                        .updateBulkWithShardNullable(ClientConfig.type(sourceKey), dataList, true)
                        .get(ClientConfig.syncWriteTimeout(sourceKey), TimeUnit.MILLISECONDS);
                ESBulkResponse esBulkResponse = ESBulkResponse.of(response);
                if (esBulkResponse.errors()) {
                    String cluster = connection.cluster().getName();
                    log.error(
                            "elasticsearch asyncBulkUpsert request fail,请联系tianwenlong确认是否集群问题，是否需要切换备用集群, "
                                    + "resource:{},cluster:{}", sourceKey, cluster);
                }
                log.info("asyncBulkUpsert operation, resource:{}, index:{}, type:{}, size:{},rsp:{}",
                        sourceKey,
                        connection.indexName(),
                        ClientConfig.type(sourceKey),
                        dataList.size(), esBulkResponse.responseJson());
            } catch (Exception e) {
                log.error(
                        "elasticsearch asyncBulkUpsert request fail resource:{},connection:{}",
                        sourceKey, connection.cluster().getName(), e);
            }
        });
    }

    protected <T> SearchResult<T> read(ElasticSearchRestQuery query, String sourceKey) {
        if (query == null || StringUtils.isBlank(sourceKey)) {
            return ModelTransferUtils.transfer(null);
        }
        try {
            RestConnection restConnection = sourceSelector.select(sourceKey);
            query.setType(ClientConfig.type(sourceKey));
            ElasticSearchRestIndex restIndex = restConnection.get();
            Response response = restIndex.search(query);
            ESSearchResponse esSearchResponse = ModelTransferUtils.mapResult(response);

            return ModelTransferUtils.transfer(esSearchResponse);
        } catch (SourceNotFoundException e) {
            log.warn("search source not found, message:{}", e.getMessage());
            throw e;
        }
    }

    protected <T> List<ThreeTuple<Object, String, Object>> buildBulkData(List<T> docs) {
        if (CollectionUtils.isEmpty(docs)) {
            return emptyList();
        }
        List<ThreeTuple<Object, String, Object>> tuples = Lists.newArrayList();
        docs.forEach(doc -> {
            try {
                Document document = ModelTransferUtils.format(doc);
                String id = document.getId();
                String partitionKey = document.getPartitionValue();
                String sourceKey = document.getSourceKey();
                String partition = String.valueOf(
                        (partitionKey.hashCode() & Integer.MAX_VALUE) % ClientConfig.getPartitionCount(sourceKey));
                ThreeTuple tuple = Tuple.tuple(partition, id, ObjectMapperUtils.fromJson(document.getSource()));
                tuples.add(tuple);
            } catch (Exception e) {
                log.error("buildBulkData发生异常 商品检索库更新失败,doc:{}", ObjectMapperUtils.toJSON(doc), e);
            }
        });
        return tuples;
    }
}
