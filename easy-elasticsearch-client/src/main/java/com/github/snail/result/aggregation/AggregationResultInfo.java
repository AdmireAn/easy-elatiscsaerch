package com.github.snail.result.aggregation;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author snail
 * Created on 2022-11-28
 * 每个聚合段
 * 聚合段可能是metric类型的也可能是bucket类型，字段冗余到一起可以避免上层过多关注类型
 */
public class AggregationResultInfo {

    private List<BucketInfo> buckets;

    private Integer value;

    public List<BucketInfo> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<BucketInfo> buckets) {
        this.buckets = buckets;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public boolean isBucket() {
        return CollectionUtils.isNotEmpty(this.buckets);
    }

    public boolean isMetric() {
        return value != null;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
