package com.github.snail.config;

import java.util.List;

/**
 * @author snail
 * Created on 2022-11-28
 */
public class ServerConfigModel {
    private String cluster;
    private int connectionSize;
    private int connectionTimeoutMills;
    private int socketTimeoutMills;
    private int ioThreadCount;
    private int configMaxRetryTimeout;
    private List<String> hostAndPorts;

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public int getConnectionSize() {
        return connectionSize;
    }

    public void setConnectionSize(int connectionSize) {
        this.connectionSize = connectionSize;
    }

    public int getConnectionTimeoutMills() {
        return connectionTimeoutMills;
    }

    public void setConnectionTimeoutMills(int connectionTimeoutMills) {
        this.connectionTimeoutMills = connectionTimeoutMills;
    }

    public int getSocketTimeoutMills() {
        return socketTimeoutMills;
    }

    public void setSocketTimeoutMills(int socketTimeoutMills) {
        this.socketTimeoutMills = socketTimeoutMills;
    }

    public int getIoThreadCount() {
        return ioThreadCount;
    }

    public void setIoThreadCount(int ioThreadCount) {
        this.ioThreadCount = ioThreadCount;
    }

    public int getConfigMaxRetryTimeout() {
        return configMaxRetryTimeout;
    }

    public void setConfigMaxRetryTimeout(int configMaxRetryTimeout) {
        this.configMaxRetryTimeout = configMaxRetryTimeout;
    }

    public List<String> getHostAndPorts() {
        return hostAndPorts;
    }

    public void setHostAndPorts(List<String> hostAndPorts) {
        this.hostAndPorts = hostAndPorts;
    }
}
