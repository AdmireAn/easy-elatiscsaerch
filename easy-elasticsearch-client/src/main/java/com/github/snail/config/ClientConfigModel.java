package com.github.snail.config;

import java.util.List;

/**
 * @author snail
 * Created on 2022-11-28
 */
public class ClientConfigModel {
    private String key;
    private String readName;
    private String writeName;
    private String type;
    private int partitionCount;
    private List<String> writeCluster;
    private List<String> readCluster;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getReadName() {
        return readName;
    }

    public void setReadName(String readName) {
        this.readName = readName;
    }

    public String getWriteName() {
        return writeName;
    }

    public void setWriteName(String writeName) {
        this.writeName = writeName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPartitionCount() {
        return partitionCount;
    }

    public void setPartitionCount(int partitionCount) {
        this.partitionCount = partitionCount;
    }

    public List<String> getWriteCluster() {
        return writeCluster;
    }

    public void setWriteCluster(List<String> writeCluster) {
        this.writeCluster = writeCluster;
    }

    public List<String> getReadCluster() {
        return readCluster;
    }

    public void setReadCluster(List<String> readCluster) {
        this.readCluster = readCluster;
    }
}
