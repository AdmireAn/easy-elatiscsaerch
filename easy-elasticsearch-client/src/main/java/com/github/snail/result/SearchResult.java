package com.github.snail.result;

import java.util.List;

import com.github.snail.result.aggregation.AggregationResultInfo;
import com.github.snail.result.aggregation.AggregationResultParser;
import com.github.snail.result.aggregation.AggregationResults;

/**
 * @author snail
 * Created on 2022-11-28
 */
public class SearchResult<T> {
    /**
     * 响应状态码
     */
    private int code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 索引命中总条数
     */
    private long total;

    /**
     * 返回结果集
     */
    private List<HitEntity<T>> entities;

    private String scrollId;

    private String aggregation;

    private AggregationResults aggregationResults;

    public long total() {
        return this.total;
    }

    public List<HitEntity<T>> entities() {
        return this.entities;
    }

    public String message() {
        return this.message;
    }

    public int code() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public void setEntities(List<HitEntity<T>> entities) {
        this.entities = entities;
    }

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }

    public String aggregation() {
        return this.aggregation;
    }

    public void setAggregation(String aggs) {
        this.aggregation = aggs;
        this.aggregationResults = AggregationResultParser.aggregationResults(aggs);
    }

    /**
     * 放心用，这个不可能空指针
     */
    public AggregationResults getAggregationResults() {
        return aggregationResults;
    }

    /**
     * 根据聚合名找到一个结果
     */
    public AggregationResultInfo aggregation(String name) {
        return this.getAggregationResults().findAggregation(name);
    }
}
