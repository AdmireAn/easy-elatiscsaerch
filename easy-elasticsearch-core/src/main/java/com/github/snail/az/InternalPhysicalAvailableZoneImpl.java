package com.github.snail.az;

import static java.util.Objects.requireNonNull;

import javax.annotation.Nonnull;

/**
 * PhysicalAvailableZone的实现，作为内部实现，不对外暴露
 *
 * @author snail
 * Created on 2022-11-28
 */
class InternalPhysicalAvailableZoneImpl implements PhysicalAvailableZone {
    private final String name;

    InternalPhysicalAvailableZoneImpl(String name) {
        this.name = requireNonNull(name).toUpperCase();
    }

    @Override
    @Nonnull
    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PhysicalAvailableZone)) {
            return false;
        }
        PhysicalAvailableZone that = (PhysicalAvailableZone) o;
        return name.equals(that.name());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}