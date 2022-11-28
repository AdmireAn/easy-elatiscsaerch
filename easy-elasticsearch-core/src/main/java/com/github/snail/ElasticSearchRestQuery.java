package com.github.snail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.index.query.QueryBuilder;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * @author snail
 * Created on 2022-11-28
 */
public class ElasticSearchRestQuery {
    private Set<String> indices;
    private String type;
    private QueryBuilder queryBuilder;
    private String queryJson;
    /**
     * 比聚合高效的去重手段，但是total返回仍然是去重前的数字
     * 只有5.x以上版本才可以使用
     */
    private String collapse;
    /**
     * 只有写入doc时带路由的场景用这个参数才有意义
     */
    private String routing;
    private String highLightJson;
    private String aggrJson;
    private String postFilterJson;
    private Collection<String> includeFields;
    private List<Map<String, ?>> sorts;
    private Map<String, String> requestParams = new HashMap<>();
    private Set<String> sourceResult;
    private int from;
    private int size;

    /**
     * 这个参数可以用来方便的扩展，可以支持任意的 rest 请求中的参数。 比如要使用 search_after,
     * ElasticSearchRestQuery restQuery = new ElasticSearchRestQuery();
     * resetQuery.setExtraParams(ImmmutableMap.of("search_after",new Object[]{1463538857,"654323"},
     * "其他参数1",new Object(),"其他参数2",new Object()))
     * 框架会把这个 map 直接序列化成 json 放到 es 的 rest 请求体中。
     * 使用
     */
    private Map<String, ?> extraParams = new HashMap<>();

    public ElasticSearchRestQuery() {
        this.indices = Sets.newHashSet();
    }

    public Set<String> getIndices() {
        return indices;
    }

    public ElasticSearchRestQuery setIndices(Set<String> thisIndices) {
        this.indices = thisIndices;
        return this;
    }

    public String getType() {
        return type;
    }

    public ElasticSearchRestQuery setType(String thisType) {
        this.type = thisType;
        return this;
    }

    public QueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

    public ElasticSearchRestQuery setQueryBuilder(QueryBuilder thisQueryBuilder) {
        this.queryBuilder = thisQueryBuilder;
        return this;
    }

    public List<Map<String, ?>> getSorts() {
        return sorts == null ? Collections.emptyList() : sorts;
    }

    public <T> ElasticSearchRestQuery setSorts(List<Map<String, T>> thisSorts) {
        if (thisSorts != null) {
            this.sorts = new ArrayList<>(thisSorts);
        }
        return this;
    }

    public Map<String, String> getRequestParams() {
        return requestParams;
    }

    public ElasticSearchRestQuery setRequestParams(Map<String, String> thisRequestParams) {
        this.requestParams = thisRequestParams;
        return this;
    }

    public Set<String> getSourceResult() {
        return sourceResult;
    }

    public ElasticSearchRestQuery setSourceResult(Set<String> thisSourceResult) {
        this.sourceResult = thisSourceResult;
        return this;
    }

    public int getFrom() {
        return from;
    }

    public ElasticSearchRestQuery setFrom(int thisFrom) {
        this.from = thisFrom;
        return this;
    }

    public int getSize() {
        return size;
    }

    public ElasticSearchRestQuery setSize(int thisSize) {
        this.size = thisSize;
        return this;
    }

    public String getHighLightJson() {
        return highLightJson;
    }

    public ElasticSearchRestQuery setHighLightJson(String thisHighLightJson) {
        this.highLightJson = thisHighLightJson;
        return this;
    }

    public String getAggrJson() {
        return aggrJson;
    }

    public ElasticSearchRestQuery setAggrJson(String thisAggrJson) {
        this.aggrJson = thisAggrJson;
        return this;
    }

    public String getPostFilterJson() {
        return postFilterJson;
    }

    public void setPostFilterJson(String postFilterJson) {
        this.postFilterJson = postFilterJson;
    }

    public Collection<String> getIncludeFields() {
        return includeFields;
    }

    public void setIncludeFields(Collection<String> includeFields) {
        this.includeFields = includeFields;
    }

    public String getQueryJson() {
        return queryJson;
    }

    public void setQueryJson(String queryJson) {
        this.queryJson = queryJson;
    }

    public String getCollapse() {
        return collapse;
    }

    public void setCollapse(String collapse) {
        this.collapse = collapse;
    }

    public String getRouting() {
        return routing;
    }

    public void setRouting(String routing) {
        this.routing = routing;
    }

    public Map<String, ?> getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(Map<String, ?> extraParams) {
        Preconditions.checkNotNull(extraParams, "extraParams 不能为空");
        this.extraParams = extraParams;
    }
}
