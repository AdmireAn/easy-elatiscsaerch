package com.github.snail.az;

import javax.annotation.Nonnull;

import com.google.common.base.Strings;

/**
 * PhysicalAvailableZone 对应 AZ 2.0提出的物理AZ概念，原先的{@link AvailableZone}定义为逻辑AZ。
 * Note: 定义为接口是为了将来变更的灵活性，业务不要定义自己的PhysicalAvailableZone实现!
 *
 * @author snail
 * Created on 2022-11-28
 */
public interface PhysicalAvailableZone extends AvailableZone {

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
     * 根据名称获取一个PhysicalAvailableZone实例.
     */
    static PhysicalAvailableZone of(String name) {
        if (Strings.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("invalid AvailableZone name: " + name);
        }
        return new InternalPhysicalAvailableZoneImpl(name);
    }
}
