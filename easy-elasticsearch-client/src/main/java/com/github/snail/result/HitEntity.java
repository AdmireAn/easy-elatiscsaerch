package com.github.snail.result;

import java.util.Map;

import com.github.snail.util.ObjectMapperUtils;

/**
 *  * 对应es查询结果的hit，一条具体的文档
 * @author snail
 * Created on 2022-11-28
 */
public class HitEntity<T> {

    /**
     * 真实的索引名
     */
    private String index;

    /**
     * 类型
     */
    private String type;

    /**
     * 路由信息
     */
    private String routing;

    /**
     * 文档id
     */
    private String id;

    /**
     * 文档内容json串
     */
    private String source;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRouting() {
        return routing;
    }

    public void setRouting(String routing) {
        this.routing = routing;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    /**
     * source解析成实体
     */
    public T source(Class<T> clazz) {
        return ObjectMapperUtils.fromJSON(this.source, clazz);
    }

    /**
     * source解析成map
     */
    public Map<String, Object> sourceMap() {
        return ObjectMapperUtils.fromJson(this.source);
    }
}
