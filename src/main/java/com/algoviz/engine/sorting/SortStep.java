package com.algoviz.engine.sorting;

import com.algoviz.engine.ExecutionStep;
import com.algoviz.model.ExecutionMetrics;
import com.algoviz.model.StepStatus;

import java.util.List;

/**
 * Immutable snapshot of the array and all highlights at one sorting step.
 * Arrays are copied on construction and on access, so a step can never be
 * modified after it has been recorded.
 */
public final class SortStep implements ExecutionStep {

    private final SortEventType type;
    private final int[] values;
    private final boolean[] sorted;
    private final int first;
    private final int second;
    private final List<SortMarker> markers;
    private final int rangeLo;
    private final int rangeHi;
    private final int[] buffer;
    private final int bufferLo;
    private final int bufferMid;
    private final int bufferLeft;
    private final int bufferRight;
    private final boolean holding;
    private final int heldValue;
    private final ExecutionMetrics metrics;
    private final int line;
    private final String explanation;
    private final StepStatus status;

    SortStep(SortEventType type, int[] values, boolean[] sorted, int first, int second,
             List<SortMarker> markers, int rangeLo, int rangeHi,
             int[] buffer, int bufferLo, int bufferMid, int bufferLeft, int bufferRight,
             boolean holding, int heldValue,
             ExecutionMetrics metrics, int line, String explanation, StepStatus status) {
        this.type = type;
        this.values = values.clone();
        this.sorted = sorted.clone();
        this.first = first;
        this.second = second;
        this.markers = List.copyOf(markers);
        this.rangeLo = rangeLo;
        this.rangeHi = rangeHi;
        this.buffer = buffer == null ? null : buffer.clone();
        this.bufferLo = bufferLo;
        this.bufferMid = bufferMid;
        this.bufferLeft = bufferLeft;
        this.bufferRight = bufferRight;
        this.holding = holding;
        this.heldValue = heldValue;
        this.metrics = metrics;
        this.line = line;
        this.explanation = explanation;
        this.status = status;
    }

    public SortEventType type() {
        return type;
    }

    public int size() {
        return values.length;
    }

    public int valueAt(int index) {
        return values[index];
    }

    public int[] values() {
        return values.clone();
    }

    public boolean isSorted(int index) {
        return sorted[index];
    }

    /** First index involved in the event (compared / swapped / written), or -1. */
    public int first() {
        return first;
    }

    /** Second index involved in the event, or -1. */
    public int second() {
        return second;
    }

    public List<SortMarker> markers() {
        return markers;
    }

    /** Active sub-range (partition or merge segment), inclusive; -1 when none. */
    public int rangeLo() {
        return rangeLo;
    }

    public int rangeHi() {
        return rangeHi;
    }

    public boolean hasRange() {
        return rangeLo >= 0;
    }

    /** Merge Sort's temporary buffer (copy of a[bufferLo..]), or null. */
    public boolean hasBuffer() {
        return buffer != null;
    }

    public int bufferLength() {
        return buffer == null ? 0 : buffer.length;
    }

    public int bufferValue(int offset) {
        return buffer[offset];
    }

    /** Array index the buffer starts at. */
    public int bufferLo() {
        return bufferLo;
    }

    /** Array index of the last element of the left half. */
    public int bufferMid() {
        return bufferMid;
    }

    /** Array index of the next element to take from the left half. */
    public int bufferLeft() {
        return bufferLeft;
    }

    /** Array index of the next element to take from the right half. */
    public int bufferRight() {
        return bufferRight;
    }

    /** Insertion Sort: true while the key has been lifted out of the array. */
    public boolean isHolding() {
        return holding;
    }

    public int heldValue() {
        return heldValue;
    }

    @Override
    public ExecutionMetrics metrics() {
        return metrics;
    }

    @Override
    public int pseudocodeLine() {
        return line;
    }

    @Override
    public String explanation() {
        return explanation;
    }

    @Override
    public StepStatus status() {
        return status;
    }
}
