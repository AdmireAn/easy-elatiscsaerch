package com.github.snail.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.snail.annotation.Doc;
import com.github.snail.annotation.Id;
import com.github.snail.annotation.PartitionFactor;
import com.github.snail.constants.StatusEnum;
import com.github.snail.index.Document;
import com.github.snail.result.ESSearchResponse;
import com.github.snail.result.HitEntity;
import com.github.snail.result.SearchResult;
import com.google.common.base.Strings;

/**
 * @author snail
 * Created on 2022-11-28
 */
public class ModelTransferUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelTransferUtils.class);

    private static final ObjectMapper OBJECTMAPPER = new ObjectMapper();

    static {
        OBJECTMAPPER.setSerializationInclusion(Include.NON_NULL);
    }

    /**
     * Es response transfer to index entity
     */
    public static <T> SearchResult<T> transfer(ESSearchResponse esResponse) {
        SearchResult<T> result = new SearchResult<>();
        result.setCode(StatusEnum.SUCCESS.getCode());
        result.setMessage(StatusEnum.SUCCESS.getMessage());
        result.setTotal(esResponse.getTotal());

        List<ObjectNode> hits = esResponse.getHits();
        List<HitEntity<T>> hitEntities = hits.stream().map(node -> {
            JsonNode idNode = node.get("_id");
            JsonNode sourceNode = node.get("_source");
            String source = sourceNode.toString();
            HitEntity<T> entity = new HitEntity<>();
            entity.setId(idNode.asText());
            entity.setSource(source);
            return entity;
        }).collect(Collectors.toList());

        result.setEntities(hitEntities);
        if (esResponse.getAggs() != null) {
            String aggregationResponse = esResponse.getAggs().toString();
            result.setAggregation(aggregationResponse);
        }
        if (!Strings.isNullOrEmpty(esResponse.getScrollId())) {
            result.setScrollId(esResponse.getScrollId());
        }
        return result;
    }

    @Nonnull
    public static ESSearchResponse mapResult(Response response) {
        if (response == null) {
            return new ESSearchResponse();
        } else {
            return new ESSearchResponse(response);
        }
    }

    /**
     * 解析索引对象中的自定义注解
     * @param object
     * @return
     */
    public static Document format(Object object) {
        Document document = new Document();
        Class klass = object.getClass();
        Field[] fields = klass.getDeclaredFields();
        Doc doc = (Doc) klass.getDeclaredAnnotation(Doc.class);
        if (doc == null) {
            throw new IllegalArgumentException("doc annotation is empty!");
        }
        if (Strings.isNullOrEmpty(doc.sourceKey())) {
            throw new IllegalArgumentException("sourceKey is empty!");
        }
        document.setSourceKey(doc.sourceKey());
        String id = null;
        if (fields.length != 0) {
            boolean gotId = false;
            boolean gotPartitionFactor = false;
            for (Field field : fields) {
                if (isConstant(field)) {
                    //过滤掉静态常亮类型属性
                    continue;
                }
                id = buildFlag(field, object, Id.class);
                if (!Strings.isNullOrEmpty(id)) {
                    document.setId(id);
                    gotId = true;
                }
                String partitionKey = buildFlag(field, object, PartitionFactor.class);
                if (!Strings.isNullOrEmpty(partitionKey)) {
                    document.setPartitionValue(partitionKey);
                    gotPartitionFactor = true;
                }
                if (gotId && gotPartitionFactor) {
                    break;
                }
            }
            if (!gotId) {
                throw new IllegalArgumentException("Id annotation is empty!");
            }
            if (!gotPartitionFactor) {
                throw new IllegalArgumentException("PartitionFactor annotation is empty!");
            }
        }
        try {
            document.setSource(OBJECTMAPPER.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            LOGGER.error("Json序列化异常:{}", id, e);
        }
        return document;
    }

    public static boolean isConstant(Field field) {
        return Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers());
    }

    /**
     * 设置id,partition等flag性信息
     */
    public static String buildFlag(Field field, Object delegate, Class anno) {
        boolean present = field.isAnnotationPresent(anno);
        try {
            if (present) {
                boolean accessible = field.isAccessible();
                Object value = getValue(field, delegate);
                if (value == null) {
                    return "";
                }
                field.setAccessible(accessible);
                return String.valueOf(value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 防止代码重复提取获取属性值的方法，使用后记得设置accessable为原来的值
     */
    public static Object getValue(Field field, Object delegate) throws IllegalAccessException {
        field.setAccessible(true);
        return field.get(delegate);
    }
}
