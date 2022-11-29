package com.github.snail.es.test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;

import com.github.snail.ElasticSearchRestQuery;
import com.github.snail.constant.SourceKey;
import com.github.snail.result.SearchResult;
import com.github.snail.service.ESSearchService;
import com.github.snail.service.impl.ESSearchServiceImpl;
import com.github.snail.util.ObjectMapperUtils;
import com.github.snail.util.SingletonFactory;
import com.google.common.collect.Lists;

/**
 * @author wangyongan
 * Created on 2022-11-26
 */
public class SnailTest1 extends BaseJunit4Test{

    private final ESSearchService esSearchService = SingletonFactory.getInstance(ESSearchServiceImpl.class);
    @Test
    public void testWriteEs() {
        Item item = new Item();
        item.setItemId(882301L);
        item.setItemName("大苹果");
        item.setPrice(994404L);
        ArrayList<Item> items = Lists.newArrayList(item);
        esSearchService.bulkUpsert(items);
    }

    @Test
    public void testReadEs() {
        BoolQueryBuilder queryBuilder = build();
        ElasticSearchRestQuery query = new ElasticSearchRestQuery()
                .setQueryBuilder(queryBuilder)
                .setFrom(0)
                .setSize(100);

        SearchResult<Item> result = esSearchService.read(query, SourceKey.EASY_WRITE_ELASTICSEARCH);
        long total = result.total();  //返回符合搜索条件的全部结果条数
        List<Item> itemSearchDocuments = result.entities()
                .stream()
                .map(hit -> hit.source(Item.class))
                .collect(Collectors.toList());
        System.out.println(total);
        System.out.println(ObjectMapperUtils.toJSON(itemSearchDocuments));
    }

    public static BoolQueryBuilder build() {
        BoolQueryBuilder queryBuilder = new BoolQueryBuilder();
        queryBuilder.filter(QueryBuilders.matchAllQuery());
        //queryBuilder.filter(QueryBuilders.rangeQuery("price").gte(0));
        return queryBuilder;
    }
}
