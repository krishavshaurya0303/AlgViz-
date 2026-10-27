package com.algoviz.model;

/**
 * One row of a complexity table, e.g. ("Search", "O(log n)", "O(n)", "Average vs. worst case").
 */
public record ComplexityEntry(String operation, String average, String worst, String note) {
}
