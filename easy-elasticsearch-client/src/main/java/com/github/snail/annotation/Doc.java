package com.github.snail.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标注在索引实体类上
 * 标识一个缩影 用于读取索引的配置
 * @author snail
 * Created on 2022-11-28
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Doc {
    String sourceKey(); //com.github.snail.constant.SourceKey#getName()
}
