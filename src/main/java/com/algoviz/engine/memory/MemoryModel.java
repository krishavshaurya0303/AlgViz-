package com.algoviz.engine.memory;

/**
 * How dynamic memory is described in the memory view.
 * <p>
 * {@link #C}: nodes come from {@code malloc(sizeof(struct node))}, pointers hold
 * addresses, and a removed node is handed back with {@code free}.
 * {@link #JAVA}: nodes come from {@code new Node(v)}, variables hold references,
 * and a removed node simply becomes unreachable until the garbage collector
 * reclaims it.
 * <p>
 * In both models the addresses are illustrative: they are derived from the node's
 * id so they stay stable across steps, but they are not real machine addresses.
 */
public enum MemoryModel {

    C("C", "NULL", "0x", "struct node"),
    JAVA("Java", "null", "@", "Node");

    private final String label;
    private final String nullText;
    private final String addressPrefix;
    private final String typeName;

    MemoryModel(String label, String nullText, String addressPrefix, String typeName) {
        this.label = label;
        this.nullText = nullText;
        this.addressPrefix = addressPrefix;
        this.typeName = typeName;
    }

    public String label() {
        return label;
    }

    public String nullText() {
        return nullText;
    }

    public String typeName() {
        return typeName;
    }

    /** Address shown for node {@code id}; {@code stride} is the block size in bytes including allocator overhead. */
    public String address(int id, int stride) {
        return addressPrefix + Integer.toHexString(0x1a40 + (id - 1) * stride);
    }

    /** Parses a stored setting; unknown values fall back to C. */
    public static MemoryModel parse(String s) {
        return "JAVA".equalsIgnoreCase(s) ? JAVA : C;
    }
}
