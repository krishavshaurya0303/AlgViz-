package com.algoviz.engine.sorting;

/** Direction of sorting. All comparisons go through {@link #outOfOrder(int, int)}. */
public enum SortOrder {
    ASCENDING("Ascending", ">", "larger"),
    DESCENDING("Descending", "<", "smaller");

    private final String label;
    private final String outOfOrderSymbol;
    private final String outOfOrderWord;

    SortOrder(String label, String outOfOrderSymbol, String outOfOrderWord) {
        this.label = label;
        this.outOfOrderSymbol = outOfOrderSymbol;
        this.outOfOrderWord = outOfOrderWord;
    }

    /**
     * True when x placed before y violates this order
     * (x &gt; y for ascending, x &lt; y for descending). Equal values are never out of order,
     * which is what keeps the stable algorithms stable.
     */
    public boolean outOfOrder(int x, int y) {
        return this == ASCENDING ? x > y : x < y;
    }

    /** "&gt;" for ascending, "&lt;" for descending. */
    public String symbol() {
        return outOfOrderSymbol;
    }

    /** "larger" for ascending, "smaller" for descending. */
    public String word() {
        return outOfOrderWord;
    }

    @Override
    public String toString() {
        return label;
    }
}
