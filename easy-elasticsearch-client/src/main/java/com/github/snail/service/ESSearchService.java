package com.github.snail.service;

import java.util.List;

import com.github.snail.ElasticSearchRestQuery;
import com.github.snail.constant.SourceKey;
import com.github.snail.result.SearchResult;

/**
 * @author snail
 * Created on 2022-11-28
 */
public interface ESSearchService {

    /**
     * 通用数据读取方法
     * <p>
     * 查询结果解析示例：
     * <p>
     * SearchResult<ItemSearchDocument> result = eSSearchService.read(query)
     * long total = result.total();  //返回符合搜索条件的全部结果条数
     * List<ItemSearchDocument> itemSearchDocuments = result.entities()
     * .stream()
     * .map(hit -> hit.source(ItemSearchDocument.class))
     * .collect(Collectors.toList());
     * <p>
     * <p>
     * termQuery方法：生成精准匹配条件，多次调用该方法产生多个 and 关系的条件
     * SearchResult：支持将返回结果转换成实体，内部使用Jackson。也可以转换成Map， 替换 hit.source(UserInfo.class) → hit.sourceMap() 即可。
     * total方法：如果想查看当前条件一共命中了多少文档，可以使用这个方法。 total >= result.entities().size()
     *
     */
    <T> SearchResult<T> read(ElasticSearchRestQuery query, SourceKey sourceKey);

    /**
     * Item索引写入 有则更新 无则插入
     * 写入数据统一收口  业务方不要写入
     *
     * @param docs 索引实体List
     */
    <T> void bulkUpsert(List<T> docs);
}
