package com.github.snail.az;

import javax.annotation.Nonnull;

import com.google.common.base.Strings;

/**
 * 可用区（AZ）的概念.可用区概念类似于IDC，目的是为提高可用性不同的AZ之间可以相互作为备份，
 * AZ是一个Region内部的多个逻辑机房，各个逻辑机房认为硬件设施相对比较独立。
 * AZ间专线带宽较大，延迟很低(个位数毫秒)，AZ间的多活相当于同城多活的概念。
 * 一个AZ内可以有一个或者多个DC
 * Note: 定义为接口是为了将来变更的灵活性，业务不要定义自己的AvailableZone实现!
 *
 * @author snail
 * Created on 2022-11-28
 */
public interface AvailableZone {

    /**
     * 可用区的名字
     */
    @Nonnull
    String name();

    default String lowerCaseName() {
        return name().toLowerCase();
    }

    default String upperCaseName() {
        return name().toUpperCase();
    }

    /**
     * 根据名称获取一个AvailableZone实例.
     */
    static AvailableZone of(String name) {
        if (Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("invalid AvailableZone name: " + name);
        }
        return new InternalAvailableZoneImpl(name);
    }
}