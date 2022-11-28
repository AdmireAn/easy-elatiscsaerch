package com.github.snail.constant;

/**
 * @author snail
 * Created on 2022-11-28
 */
public enum SourceKey {

    // 测试索引
    EASY_WRITE_ELASTICSEARCH("easy_write_elasticsearch");
    private String key;

    SourceKey(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }
}
