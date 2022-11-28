package com.github.snail.util;

import static com.fasterxml.jackson.core.JsonFactory.Feature.INTERN_FIELD_NAMES;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.snail.exception.UncheckedJsonProcessingException;

/**
 * @author snail
 * Created on 2022-11-28
 */
public final class ObjectMapperUtils {

    private static final String EMPTY_JSON = "{}";
    private static final String EMPTY_ARRAY_JSON = "[]";
    /**
     * disable INTERN_FIELD_NAMES, 解决GC压力大、内存泄露的问题
     *
     * @see <a href="https://jira.corp.kuaishou.com/browse/INFRAJAVA-552">JIRA</a>
     */
    private static final ObjectMapper MAPPER = new ObjectMapper(new JsonFactory().disable(INTERN_FIELD_NAMES));

    static {
        MAPPER.disable(FAIL_ON_UNKNOWN_PROPERTIES);
        MAPPER.enable(ALLOW_UNQUOTED_CONTROL_CHARS);
        MAPPER.enable(ALLOW_COMMENTS);
        /*
          Benchmark                                                Mode  Cnt    Score   Error   Units
          JasksonAfterBurnerBenchmark.photoFeedViewFast           thrpt    2   34.302          ops/ms
          JasksonAfterBurnerBenchmark.photoFeedViewNormal         thrpt    2   28.017          ops/ms
          JasksonAfterBurnerBenchmark.requestPropertyFast         thrpt    2  663.840          ops/ms
          JasksonAfterBurnerBenchmark.requestPropertyNormal       thrpt    2  565.343          ops/ms
          JasksonAfterBurnerBenchmark.requestPropertyParseFast    thrpt    2  720.691          ops/ms
          JasksonAfterBurnerBenchmark.requestPropertyParseNormal  thrpt    2  412.315          ops/ms

          benchmark by lijie02
         */
        //        MAPPER.registerModule(new AfterburnerModule().setUseValueClassLoader(false));
    }


    public static String toJSON(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new UncheckedJsonProcessingException(e);
        }
    }

    public static <T> T fromJSON(@Nullable String json, Class<T> valueType) {
        if (json == null) {
            return null;
        }
        try {
            return MAPPER.readValue(json, valueType);
        } catch (IOException e) {
            throw wrapException(e);
        }
    }

    private static RuntimeException wrapException(IOException e) {
        if (e instanceof JsonProcessingException) {
            return new UncheckedJsonProcessingException((JsonProcessingException) e);
        } else {
            return new UncheckedIOException(e);
        }
    }

    /**
     * 输出格式化好的json
     * 请不要在输出log时使用
     *
     * 一般只用于写结构化数据到ZooKeeper时使用（为了更好的可读性）
     */
    public static String toPrettyJson(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new UncheckedJsonProcessingException(e);
        }
    }

    public static Map<String, Object> fromJson(String string) {
        return fromJSON(string, Map.class, String.class, Object.class);
    }

    /**
     * use {@link #fromJson(String)} instead
     */
    public static <K, V, T extends Map<K, V>> T fromJSON(String json, Class<? extends Map> mapType,
            Class<K> keyType, Class<V> valueType) {
        if (StringUtils.isEmpty(json)) {
            json = EMPTY_JSON;
        }
        try {
            return MAPPER.readValue(json,
                    defaultInstance().constructMapType(mapType, keyType, valueType));
        } catch (IOException e) {
            throw wrapException(e);
        }
    }

}
