package com.github.snail.client;

import org.elasticsearch.client.RestClient;

import com.github.snail.failover.SimpleFailover;
import com.google.common.net.HostAndPort;

/**
 * @author snail
 * Created on 2022-11-28
 */
public class ElasticsearchRestClient {
    private final RestClient restClient;
    private final HostAndPort hostAndPort;
    private SimpleFailover<ElasticsearchRestClient> failover;

    public ElasticsearchRestClient(RestClient restClient, HostAndPort hostAndPort) {
        this.restClient = restClient;
        this.hostAndPort = hostAndPort;
    }

    public RestClient getRestClient() {
        return restClient;
    }

    public HostAndPort getHostAndPort() {
        return hostAndPort;
    }

    public void setFailover(SimpleFailover<ElasticsearchRestClient> failover) {
        this.failover = failover;
    }

    public SimpleFailover<ElasticsearchRestClient> getFailover() {
        return failover;
    }

    @Override
    public String toString() {
        return "ElasticsearchRestClient{" + "restClient=" + restClient + ", hostAndPort="
                + hostAndPort + '}';
    }
}
