package com.algoviz.engine.sorting;

import com.algoviz.config.AppConfig;

import java.util.Arrays;
import java.util.Objects;

/** Validated input for a sorting run. The array is defensively copied. */
public final class SortInput {

    private final int[] values;
    private final SortOrder order;

    public SortInput(int[] values, SortOrder order) {
        Objects.requireNonNull(values, "values");
        if (values.length > AppConfig.SORT_MAX_SIZE) {
            throw new IllegalArgumentException("At most " + AppConfig.SORT_MAX_SIZE + " values are supported");
        }
        this.values = values.clone();
        this.order = Objects.requireNonNull(order, "order");
    }

    public int[] values() {
        return values.clone();
    }

    public int size() {
        return values.length;
    }

    public SortOrder order() {
        return order;
    }

    @Override
    public String toString() {
        return Arrays.toString(values) + " " + order;
    }
}
