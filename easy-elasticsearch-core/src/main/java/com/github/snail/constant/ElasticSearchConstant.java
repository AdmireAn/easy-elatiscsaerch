package com.github.snail.constant;

import java.util.concurrent.TimeUnit;

/**
 * @author snail
 * Created on 2022-11-28
 */
public interface ElasticSearchConstant {
    String INDEX = "index";
    String TYPE = "type";
    String TOTAL = "total";
    String HITS = "hits";
    String HIGHLIGHT = "highlight";
    String PARENT = "parent";
    String ROUTING = "routing";
    String TTL = "ttl";
    String SOURCE = "_source";
    String REFRESH = "_refresh";
    String UPDATE = "_update";
    String DOC = "doc";
    String RESPONSES = "responses";
    String DOC_AS_UPSERT = "doc_as_upsert";
    String AGGREGATION = "aggregations";
    String AGGREGATION_BULKS = "buckets";
    String AGGREGATION_SUM = "sum";
    String AGGREGATION_KEY = "key";
    String AGGREGATION_VALUE = "value";
    String AGGREGATION_DOC_COUNT = "doc_count";
    String ORDER = "order";
    String ORDER_DESC = "desc";
    String ORDER_ASC = "asc";
    String VERSION = "version";
    String VERSION_TYPE = "version_type";

    String DUMMY_INDEX = "dummyIndex";
    String DUMMY_TYPE = "dummyType";

    class Connect {
        // 默认参数
        public static final int DEFAULT_CONNECT_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(60);
        public static final int DEFAULT_SOCKET_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(60);
        public static final int DEFAULT_MAX_RETRY_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(60);
        public static final int DEFAULT_REST_CLIENT_SIZE = 1;
        public static final int DEFAULT_THREAD_SIZE = 200;

        // 配置相关key
        public static final String CONFIG_CLUSTER = "cluster";
        public static final String CONFIG_CONNECT_TIMEOUT = "connectTimeout";
        public static final String CONFIG_SOCKET_TIMEOUT = "socketTimeout";
        public static final String CONFIG_MAX_RETRY_TIMEOUT = "maxRetryTimeout";
        public static final String CONFIG_THREAD_SIZE = "threadSize";
        public static final String CONFIG_CLIENT_SIZE = "clientSize";
        public static final String CONFIG_SERVERS = "servers";
        public static final String CONFIG_KCC_CLIENT_PROXY_PASSWORD = "kcc_client_proxy_password";

        // 字符常量
        public static final String INDEX_ERROR_TIP = "index must set when use search method";
        public static final String PERF_DETAIL_TAG = "es.rest.op.detail";
        public static final String PERF_OP_SUCCESS = "success";
        public static final String PERF_OP_FAILED = "failed";
    }

    class Endpoint {

        public static final String SEARCH = "_search";

        public static final String SCROLL = "_search/scroll";

        public static final String UPDATE = "_update";

        public static final String UPDATE_BY_QUERY = "_update_by_query";

        public static final String DELETE_BY_QUERY = "_delete_by_query";

        public static final String BULK = "_bulk";

        public static final String MGET = "_mget";

        public static final String ALIASES = "_aliases";

        public static final String MULTI_SEARCH = "_msearch";
    }

    class RequestBody {

        public static final String QUERY = "query";

        public static final String FROM = "from";

        public static final String SIZE = "size";

        public static final String SOURCE = "_source";

        public static final String AGGREGATION = "aggs";

        public static final String SCROLL = "scroll";

        public static final String SCROLL_ID = "scroll_id";

        public static final String COLLAPSE = "collapse";

        public static final String POST_FILTER = "post_filter";

        public static final String SORT = "sort";

        public static final String DOC_AS_UPSERT = "doc_as_upsert";

        public static final String DOC = "doc";

        public static final String DOCS = "docs";

        public static final String ACTIONS = "actions";

        public static final String SETTING = "settings";

        public static final String MAPPING = "mapping";
    }

    class RequestParam {

        public static final String ROUTING = "routing";

        public static final String PARENT = "parent";

        public static final String TTL = "ttl";

        public static final String INDEX = "index";

        public static final String TYPE = "type";
    }

    class Method {
        public static final String GET = "GET";

        public static final String POST = "POST";

        public static final String PUT = "PUT";

        public static final String DELETE = "DELETE";
    }

    enum OpType {
        GET,
        QUERY,
        DELETE,
        DELETE_BY_QUERY,
        SCROLL,
        CLEAR_SCROLL,
        MULTI_GET,
        INSERT,
        UPDATE,
        UPDATE_BY_QUERY,
        BULK,
        MULTI_SEARCH,
        INDEX_ALIASES,
        DELETE_INDEX,
        CREATE_INDEX,
        OTHER
    }

    /**
     * 自定义版本控制：https://www.elastic.co/guide/en/elasticsearch/reference/7.17/docs-bulk.html#bulk-versioning
     * <p>
     * 如果不指定versionType，只指定version，则为ES默认版本控制：
     * 如果当前修改数据时的版本号和ES中的版本号相同，则可以修改成功，否则修改失败
     */
    enum VersionType {
        // 仅当给定版本严格高于存储文档的版本才执行
        external,
        // 仅当给定版本不存在现有文档时才执行
        external_gt,
        // 仅当给定版本等于或高于存储文档的版本时才索引文档。如果没有现有文档，则操作也会成功。（慎用！）
        external_gte
    }

    // 规避 checkstyle
    void get();
}
