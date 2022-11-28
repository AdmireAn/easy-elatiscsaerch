package com.github.snail.result.aggregation;


import static com.github.snail.util.ObjectMapperUtils.toJSON;

import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.snail.util.ObjectMapperUtils;


/**
 * @author snail
 * Created on 2022-11-28
 * 聚合结果解析构造
 */
public class AggregationResultParser {

    private static final Logger logger = LoggerFactory.getLogger(AggregationResultParser.class);

    public static AggregationResults aggregationResults(String aggJson) {
        AggregationResults results = new AggregationResults();
        if (StringUtils.isBlank(aggJson)) {
            return results;
        }
        Map<String, Object> aggregationRootMap = ObjectMapperUtils.fromJson(aggJson);
        if (MapUtils.isEmpty(aggregationRootMap)) {
            return results;
        }
        for (Map.Entry<String, Object> entry : aggregationRootMap.entrySet()) {
            String name = entry.getKey();
            Map<String, Object> valueMap = (Map<String, Object>) entry.getValue();
            AggregationResultInfo resultInfo =
                    ObjectMapperUtils.fromJSON(toJSON(valueMap), AggregationResultInfo.class);
            results.addAggregationInfo(name, resultInfo);
        }
        return results;
    }
}
