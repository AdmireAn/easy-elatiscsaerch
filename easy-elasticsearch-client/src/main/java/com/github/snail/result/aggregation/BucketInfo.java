package com.github.snail.result.aggregation;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author snail
 * Created on 2022-11-28
 */
public class BucketInfo {

    private String key;

    @JsonProperty("doc_count")
    private String docCount;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDocCount() {
        return docCount;
    }

    public void setDocCount(String docCount) {
        this.docCount = docCount;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
