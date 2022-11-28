package com.github.snail.index;

/**
 * @author snail
 * Created on 2022-11-28
 */

public class Document {
    private String sourceKey;

    private String source;

    private String id; //索引id

    private String partitionValue;

    public String getSourceKey() {
        return sourceKey;
    }

    public void setSourceKey(String sourceKey) {
        this.sourceKey = sourceKey;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPartitionValue() {
        return partitionValue;
    }

    public void setPartitionValue(String partitionValue) {
        this.partitionValue = partitionValue;
    }
}
