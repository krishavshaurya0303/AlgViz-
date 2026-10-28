package com.algoviz.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A real-language listing (C) of an algorithm, tied line by line to its pseudocode.
 * {@code marks.get(i)} holds the pseudocode lines that C line {@code i} carries out,
 * so the step that highlights pseudocode line {@code p} can highlight the matching C lines.
 */
public record CodeListing(List<String> lines, List<Set<Integer>> marks) {

    public CodeListing {
        lines = List.copyOf(lines);
        marks = List.copyOf(marks);
        if (lines.size() != marks.size()) {
            throw new IllegalArgumentException("one mark set per line");
        }
    }

    /** Zero-based C lines that implement pseudocode line {@code pseudoLine}; empty when none do. */
    public List<Integer> linesFor(int pseudoLine) {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < marks.size(); i++) {
            if (marks.get(i).contains(pseudoLine)) {
                out.add(i);
            }
        }
        return out;
    }
}
