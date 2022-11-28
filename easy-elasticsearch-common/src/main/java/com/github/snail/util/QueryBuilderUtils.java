package com.github.snail.util;

import org.elasticsearch.index.query.QueryBuilder;

import com.google.common.base.Preconditions;

/**
 * @author snail
 * Created on 2022-11-28
 */
public class QueryBuilderUtils {

    public static String buildQueryJSON(QueryBuilder queryBuilder) {
        Preconditions.checkArgument(queryBuilder != null);
        return queryBuilder.buildAsBytes().toUtf8();
    }
}
