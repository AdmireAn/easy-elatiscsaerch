package com.github.snail.result;

import static com.github.snail.constant.ElasticSearchConstant.AGGREGATION;
import static com.github.snail.constant.ElasticSearchConstant.HITS;
import static com.github.snail.constant.ElasticSearchConstant.TOTAL;
import static com.github.snail.util.MoreSuppliers.lazy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.elasticsearch.client.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.snail.tuple.Tuple;
import com.github.snail.tuple.TwoTuple;
import com.github.snail.util.ObjectMapperUtils;

/**
 * @author snail
 * Created on 2022-11-28
 */
public class ESSearchResponse {

    private final Supplier<Long> took;
    private final Supplier<Long> total;
    private final Supplier<List<ObjectNode>> hits;
    private final Supplier<String> scrollId;
    private final Supplier<JsonNode> aggs;

    private final Supplier<Integer> status;
    private final Supplier<String> content;

    public ESSearchResponse() {
        content = lazy(() -> "");
        scrollId = lazy(() -> StringUtils.EMPTY);
        total = lazy(() -> 0L);
        took = lazy(() -> 0L);
        hits = lazy(Collections::emptyList);
        status = lazy(() -> HttpStatus.SC_BAD_REQUEST);
        aggs = lazy(() -> ObjectMapperUtils.fromJSON("{}", JsonNode.class));
    }

    /**
     * 使用
     */
    public ESSearchResponse(final Response response) {
        TwoTuple<Integer, String> resultTuple = convertFullResponse(response);
        if (resultTuple == null) {
            content = lazy(() -> "");
            took = lazy(() -> 0L);
            total = lazy(() -> 0L);
            scrollId = lazy(() -> StringUtils.EMPTY);
            hits = lazy(ArrayList::new);
            status = lazy(() -> HttpStatus.SC_BAD_REQUEST);
            this.aggs = lazy(() -> ObjectMapperUtils.fromJSON("{}", JsonNode.class));
        } else {
            String data = resultTuple.getSecond();
            this.content = lazy(() -> data);
            this.status = lazy(() -> resultTuple.getFirst());
            ObjectNode object = ObjectMapperUtils.fromJSON(data, ObjectNode.class);
            if (object.has(HITS)) {
                JsonNode outerHit = object.get(HITS);
                total = lazy(() -> outerHit.get(TOTAL).asLong());
                hits = lazy(() -> {
                    List<ObjectNode> result = new ArrayList<>();
                    JsonNode jsonArray = outerHit.withArray(HITS);
                    for (final Object obj : jsonArray) {
                        ObjectNode hit = (ObjectNode) obj;
                        result.add(hit);
                    }
                    return result;
                });

            } else {
                total = lazy(() -> 0L);
                hits = lazy(ArrayList::new);
            }
            if (object.has(AGGREGATION)) {
                this.aggs = lazy(() -> object.get(AGGREGATION));
            } else {
                this.aggs = lazy(() -> ObjectMapperUtils.fromJSON("{}", JsonNode.class));
            }
            if (object.has("took")) {
                this.took = lazy(() -> object.get("took").asLong());
            } else {
                this.took = lazy(() -> 0L);
            }
            if (object.hasNonNull("_scroll_id")) {
                scrollId = lazy(() -> object.get("_scroll_id").asText());
            } else {
                scrollId = lazy(() -> StringUtils.EMPTY);
            }
        }
    }

    public boolean notOk() {
        return this.getStatus() != HttpStatus.SC_CREATED
                && this.getStatus() != HttpStatus.SC_OK;
    }

    public static TwoTuple<Integer, String> convertFullResponse(Response response) {
        try {
            int statusCode = response.getStatusLine().getStatusCode();
            String content = IOUtils.toString(response.getEntity().getContent(), Charsets.UTF_8);
            return Tuple.tuple(statusCode, content);
        } catch (IOException e) {
            return null;
        }
    }

    public long getTook() {
        return took.get();
    }

    public long getTotal() {
        return total.get();
    }

    public String getScrollId() {
        return scrollId.get();
    }

    public List<ObjectNode> getHits() {
        return hits.get();
    }

    public int getStatus() {
        return status.get();
    }

    public JsonNode getAggs() {
        return aggs.get();
    }

    public String getContent() {
        return content.get();
    }
}
