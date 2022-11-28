package com.github.snail.result.aggregation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;

/**
 * @author snail
 * Created on 2022-11-28
 * 总的聚合结果
 */
public class AggregationResults {

    private Map<String, AggregationResultInfo> aggregationMap = Maps.newHashMap();

    public AggregationResults addAggregationInfo(String name, AggregationResultInfo info) {
        if (info == null) {
            return this;
        }
        aggregationMap.put(name, info);
        return this;
    }

    /**
     * 获取全部分组统计的结果
     */
    public List<AggregationResultInfo> bucketResults() {
        return aggregationMap.values()
                .stream()
                .filter(AggregationResultInfo::isBucket)
                .collect(Collectors.toList());
    }

    /**
     * 获取全部聚合计算的结果
     */
    public List<AggregationResultInfo> metricResults() {
        return aggregationMap.values()
                .stream()
                .filter(AggregationResultInfo::isMetric)
                .collect(Collectors.toList());
    }

    /**
     * 找到一个聚合结果
     */
    public AggregationResultInfo findAggregation(String name) {
        return aggregationMap.get(name);
    }

    public Map<String, AggregationResultInfo> getAllAggregationResults() {
        return this.aggregationMap;
    }

    /**
     * 直接获取分组统计的结果，开箱即用
     */
    public Map<Long, Integer> getGroupByLong(String name) {
        AggregationResultInfo groupByInfo = findAggregation(name);
        if (groupByInfo == null || groupByInfo.isMetric()) {
            return Collections.emptyMap();
        }
        return groupByInfo.getBuckets().stream()
                .collect(Collectors.toMap(bucket -> Long.parseLong(bucket.getKey()),
                        bucket -> Integer.parseInt(bucket.getDocCount())));
    }

    /**
     * 直接获取分组统计的结果，开箱即用
     */
    public Map<Integer, Integer> getGroupByInteger(String name) {
        AggregationResultInfo groupByInfo = findAggregation(name);
        if (groupByInfo == null || groupByInfo.isMetric()) {
            return Collections.emptyMap();
        }
        return groupByInfo.getBuckets().stream()
                .collect(Collectors.toMap(bucket -> Integer.parseInt(bucket.getKey()),
                        bucket -> Integer.parseInt(bucket.getDocCount())));
    }

}
