package com.github.snail.result;

import static com.github.snail.util.MoreSuppliers.lazy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.snail.tuple.Tuple;
import com.github.snail.tuple.TwoTuple;
import com.github.snail.util.ObjectMapperUtils;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

/**
 * 用来构建批量操作文档的返回结果
 * 包括失败的id，写入耗时等等
 * @author snail
 * Created on 2022-11-28
 */
public final class ESBulkResponse {

    private static final Logger LOGGER = LoggerFactory.getLogger(ESBulkResponse.class);

    private static final String TOOK = "took";
    private static final String ERRORS = "errors";
    private static final String ITEMS = "items";
    private static final String INDEX_TEXT = "index";
    private static final String UPDATE_TEXT = "update";
    private static final String DELETE_TEXT = "delete";
    private static final String ID = "_id";
    private static final String INDEX = "_index";
    private static final String STATUS = "status";
    private static final String ERROR = "error";
    private static final String REASON = "reason";

    private static final int UPDATE = 200;
    private static final int CREATE = 201;
    private static final int FAIL = 400;


    private final Supplier<Long> took;

    private final Supplier<Boolean> errors;

    private final Supplier<List<ObjectNode>> items;


    private String responseJson;

    private ESBulkResponse(Response response) {
        this.responseJson = convertResponse(response);
        ObjectNode object = ObjectMapperUtils.fromJSON(responseJson, ObjectNode.class);
        JsonNode itemsNode = object.get(ITEMS);
        this.took = lazy(() -> object.get(TOOK).asLong());
        this.errors = lazy(() -> object.get(ERRORS).asBoolean());
        this.items = lazy(() -> {
            List<ObjectNode> result = new ArrayList<>();
            for (Object obj : itemsNode) {
                result.add((ObjectNode) obj);
            }
            return result;
        });
    }

    private ESBulkResponse(String responseJson) {
        ObjectNode object = ObjectMapperUtils.fromJSON(responseJson, ObjectNode.class);
        JsonNode itemsNode = object.get(ITEMS);
        this.took = lazy(() -> object.get(TOOK).asLong());
        this.errors = lazy(() -> object.get(ERRORS).asBoolean());
        this.items = lazy(() -> {
            List<ObjectNode> result = new ArrayList<>();
            for (Object obj : itemsNode) {
                result.add((ObjectNode) obj);
            }
            return result;
        });
    }

    public static ESBulkResponse of(Response response) {
        return new ESBulkResponse(response);
    }

    public static ESBulkResponse of(String response) {
        return new ESBulkResponse(response);
    }


    private String convertResponse(Response response) {
        if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                return IOUtils.toString(response.getEntity().getContent(), Charsets.UTF_8);
            } catch (IOException e) {
                LOGGER.warn("convert es response failed. {}", e.getMessage());
            }
        }
        return StringUtils.EMPTY;
    }

    public long took() {
        return took.get();
    }

    public boolean errors() {
        return errors.get();
    }

    public List<TwoTuple<String, String>> failures() {
        List<ObjectNode> itemNodes = this.items.get();
        if (itemNodes.isEmpty()) {
            return Lists.newArrayList();
        }
        return itemNodes.stream()
                .filter(this::fail)
                .map(jsonNodes -> {
                    JsonNode indexNode = jsonNodes.get(INDEX_TEXT);
                    JsonNode updateNode = jsonNodes.get(UPDATE_TEXT);
                    JsonNode delNode = jsonNodes.get(DELETE_TEXT);
                    JsonNode node = indexNode;
                    if (updateNode != null) {
                        node = updateNode;
                    }
                    if (delNode != null) {
                        node = delNode;
                    }
                    if (node == null) {
                        return Tuple.tuple("unkown id", "unkown reason");
                    }
                    String id = node.get(ID).asText();
                    JsonNode errorNode = node.get(ERROR);
                    String reason = errorNode.get(REASON).asText();
                    return Tuple.tuple(id, reason);
                })
                .collect(Collectors.toList());
    }

    public String responseJson() {
        return this.responseJson;
    }

    private boolean fail(ObjectNode jsonNodes) {
        JsonNode indexNode = jsonNodes.get(INDEX_TEXT);
        JsonNode updateNode = jsonNodes.get(UPDATE_TEXT);
        JsonNode delNode = jsonNodes.get(DELETE_TEXT);
        if (indexNode != null) {
            int indexStatus = indexNode.get(STATUS).asInt();
            return indexStatus != CREATE && indexStatus != UPDATE;
        }
        if (updateNode != null) {
            int updateStatus = updateNode.get(STATUS).asInt();
            return updateStatus != CREATE && updateStatus != UPDATE;
        }
        if (delNode != null) {
            int delStatus = delNode.get(STATUS).asInt();
            return delStatus != CREATE && delStatus != UPDATE;
        }
        return false;
    }
}
