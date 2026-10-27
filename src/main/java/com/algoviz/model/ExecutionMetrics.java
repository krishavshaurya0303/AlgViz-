package com.algoviz.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable snapshot of the counters at one execution step.
 * Because every step stores its own snapshot, stepping backward restores the
 * counters exactly — no "un-counting" logic is needed.
 */
public final class ExecutionMetrics {

    private static final ExecutionMetrics EMPTY = new ExecutionMetrics(new EnumMap<>(MetricType.class));

    private final EnumMap<MetricType, Long> values;

    private ExecutionMetrics(EnumMap<MetricType, Long> values) {
        this.values = values;
    }

    public static ExecutionMetrics empty() {
        return EMPTY;
    }

    /** Returns a copy of this snapshot with one metric set. */
    public ExecutionMetrics with(MetricType type, long value) {
        EnumMap<MetricType, Long> copy = new EnumMap<>(MetricType.class);
        copy.putAll(values);
        copy.put(type, value);
        return new ExecutionMetrics(copy);
    }

    public long get(MetricType type) {
        return values.getOrDefault(type, 0L);
    }

    public boolean has(MetricType type) {
        return values.containsKey(type);
    }

    public Map<MetricType, Long> asMap() {
        return Collections.unmodifiableMap(values);
    }

    /** Mutable builder used by recorders while an algorithm runs. */
    public static final class Counter {
        private final EnumMap<MetricType, Long> values = new EnumMap<>(MetricType.class);

        public Counter track(MetricType... types) {
            for (MetricType t : types) {
                values.putIfAbsent(t, 0L);
            }
            return this;
        }

        public void increment(MetricType type) {
            add(type, 1);
        }

        public void add(MetricType type, long delta) {
            values.merge(type, delta, Long::sum);
        }

        public void set(MetricType type, long value) {
            values.put(type, value);
        }

        public long get(MetricType type) {
            return values.getOrDefault(type, 0L);
        }

        public ExecutionMetrics snapshot() {
            return new ExecutionMetrics(new EnumMap<>(values));
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ExecutionMetrics other && values.equals(other.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(values);
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
