package com.algoviz.engine.sorting;

import com.algoviz.model.ExecutionMetrics;
import com.algoviz.model.MetricType;
import com.algoviz.model.StepStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Instrumented array used by every sorting algorithm.
 * <p>
 * Algorithms never touch the raw array: every comparison, swap and write goes
 * through this class, which (1) performs the operation, (2) updates the
 * counters and (3) records an immutable {@link SortStep}. This guarantees that
 * counters, highlights and explanations always match what really happened.
 */
public final class SortRecorder {

    /** Builds the explanation text for a comparison once its result is known. */
    @FunctionalInterface
    public interface CompareExplainer {
        String explain(boolean outOfOrder, int x, int y);
    }

    private final int[] a;
    private final boolean[] sorted;
    private final SortOrder order;
    private final ExecutionMetrics.Counter counter = new ExecutionMetrics.Counter()
            .track(MetricType.COMPARISONS, MetricType.SWAPS, MetricType.WRITES);
    private final List<SortStep> steps = new ArrayList<>();

    // Persistent decorations: they stay visible on every step until cleared.
    private final Map<String, SortMarker> markers = new LinkedHashMap<>();
    private int rangeLo = -1;
    private int rangeHi = -1;
    private int[] buffer;
    private int bufferLo = -1;
    private int bufferMid = -1;
    private int bufferLeft = -1;
    private int bufferRight = -1;
    private boolean holding;
    private int heldValue;

    public SortRecorder(int[] values, SortOrder order) {
        this.a = values.clone();
        this.sorted = new boolean[values.length];
        this.order = order;
    }

    // ----------------------------------------------------------- read access

    public int size() {
        return a.length;
    }

    /** Reading a value is not an operation that is counted. */
    public int get(int index) {
        return a[index];
    }

    public SortOrder order() {
        return order;
    }

    /** "&gt;" or "&lt;" depending on the order. */
    public String gt() {
        return order.symbol();
    }

    /** "larger" or "smaller" depending on the order. */
    public String bigger() {
        return order.word();
    }

    public List<SortStep> steps() {
        return steps;
    }

    // ------------------------------------------------------------ operations

    /** Compares a[i] with a[j]: true when a[i] must come after a[j]. Records one COMPARE step. */
    public boolean compare(int i, int j, int line, CompareExplainer explainer) {
        int x = a[i];
        int y = a[j];
        counter.increment(MetricType.COMPARISONS);
        boolean out = order.outOfOrder(x, y);
        record(SortEventType.COMPARE, i, j, line, explainer.explain(out, x, y), StepStatus.ACTIVE);
        return out;
    }

    /** Compares a[i] with the held key: true when a[i] must come after the key. */
    public boolean compareWithHeld(int i, int line, CompareExplainer explainer) {
        int x = a[i];
        counter.increment(MetricType.COMPARISONS);
        boolean out = order.outOfOrder(x, heldValue);
        record(SortEventType.COMPARE, i, -1, line, explainer.explain(out, x, heldValue), StepStatus.ACTIVE);
        return out;
    }

    /**
     * Compares the two buffer heads during a merge: true when the left head must
     * come after the right head (equal values are not out of order: stability).
     */
    public boolean compareBufferHeads(int line, CompareExplainer explainer) {
        int x = buffer[bufferLeft - bufferLo];
        int y = buffer[bufferRight - bufferLo];
        counter.increment(MetricType.COMPARISONS);
        boolean out = order.outOfOrder(x, y);
        record(SortEventType.COMPARE, -1, -1, line, explainer.explain(out, x, y), StepStatus.ACTIVE);
        return out;
    }

    /** Exchanges a[i] and a[j]. Counts one swap and two writes. */
    public void swap(int i, int j, int line, String explanation) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
        counter.increment(MetricType.SWAPS);
        counter.add(MetricType.WRITES, 2);
        record(SortEventType.SWAP, i, j, line, explanation, StepStatus.WARNING);
    }

    /** Assigns a[i] = value. Counts one write. */
    public void write(int i, int value, int line, String explanation) {
        a[i] = value;
        counter.increment(MetricType.WRITES);
        record(SortEventType.WRITE, i, -1, line, explanation, StepStatus.WARNING);
    }

    public void markSorted(int i, int line, String explanation) {
        sorted[i] = true;
        record(SortEventType.MARK_SORTED, i, -1, line, explanation, StepStatus.SUCCESS);
    }

    /** Records a step that changes no data (e.g. choosing a pivot). */
    public void note(int line, String explanation) {
        record(SortEventType.NOTE, -1, -1, line, explanation, StepStatus.INFO);
    }

    public void note(int first, int second, int line, String explanation) {
        record(SortEventType.NOTE, first, second, line, explanation, StepStatus.INFO);
    }

    void start(String explanation) {
        record(SortEventType.START, -1, -1, -1, explanation, StepStatus.INFO);
    }

    /** Marks every element as final and records the DONE step. */
    void finish() {
        markers.clear();
        clearRange();
        clearBuffer();
        holding = false;
        java.util.Arrays.fill(sorted, true);
        record(SortEventType.DONE, -1, -1, -1,
                "Done. The array is sorted in " + order.toString().toLowerCase() + " order.",
                StepStatus.SUCCESS);
    }

    // ----------------------------------------------------------- decorations

    public void setMarker(String label, int index, MarkerRole role) {
        markers.put(label, new SortMarker(index, label, role));
    }

    public void clearMarker(String label) {
        markers.remove(label);
    }

    public void clearMarkers() {
        markers.clear();
    }

    public void setRange(int lo, int hi) {
        rangeLo = lo;
        rangeHi = hi;
    }

    public void clearRange() {
        rangeLo = -1;
        rangeHi = -1;
    }

    /** Copies a[lo..hi] into the merge buffer; left half is lo..mid. */
    public void loadBuffer(int lo, int mid, int hi) {
        buffer = java.util.Arrays.copyOfRange(a, lo, hi + 1);
        bufferLo = lo;
        bufferMid = mid;
        bufferLeft = lo;
        bufferRight = mid + 1;
    }

    /** Value at an absolute array index inside the buffer. */
    public int bufferAt(int arrayIndex) {
        return buffer[arrayIndex - bufferLo];
    }

    public void setBufferHeads(int left, int right) {
        bufferLeft = left;
        bufferRight = right;
    }

    public void clearBuffer() {
        buffer = null;
        bufferLo = bufferMid = bufferLeft = bufferRight = -1;
    }

    public void hold(int value) {
        holding = true;
        heldValue = value;
    }

    public void release() {
        holding = false;
    }

    public int heldValue() {
        return heldValue;
    }

    // ---------------------------------------------------------------- helpers

    private void record(SortEventType type, int first, int second, int line, String text, StepStatus status) {
        steps.add(new SortStep(type, a, sorted, first, second, new ArrayList<>(markers.values()),
                rangeLo, rangeHi, buffer, bufferLo, bufferMid, bufferLeft, bufferRight,
                holding, heldValue, counter.snapshot(), line, text, status));
    }
}
