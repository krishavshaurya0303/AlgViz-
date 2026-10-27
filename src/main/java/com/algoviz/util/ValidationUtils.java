package com.algoviz.util;

import java.util.ArrayList;
import java.util.List;

/** Parsing and validation of user-typed values. */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    /** Parses one integer within [min, max]. */
    public static int parseInt(String text, String fieldName, int min, int max) throws InvalidInputException {
        if (text == null || text.isBlank()) {
            throw new InvalidInputException("Enter a value for " + fieldName + ".");
        }
        String t = text.trim();
        int value;
        try {
            value = Integer.parseInt(t);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("\"" + t + "\" is not a whole number.");
        }
        if (value < min || value > max) {
            throw new InvalidInputException(fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1)
                    + " must be between " + min + " and " + max + ".");
        }
        return value;
    }

    /**
     * Parses a list such as "5, 3 8,-2". Commas, semicolons and whitespace all separate values.
     */
    public static int[] parseIntList(String text, int minCount, int maxCount, int min, int max)
            throws InvalidInputException {
        if (text == null || text.isBlank()) {
            throw new InvalidInputException("Enter some numbers, e.g. 5, 3, 8, 1.");
        }
        String[] tokens = text.trim().split("[\\s,;]+");
        List<Integer> values = new ArrayList<>();
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            int v;
            try {
                v = Integer.parseInt(token);
            } catch (NumberFormatException e) {
                throw new InvalidInputException("\"" + token + "\" is not a whole number.");
            }
            if (v < min || v > max) {
                throw new InvalidInputException("Value " + v + " is outside the allowed range " + min + " to " + max + ".");
            }
            values.add(v);
        }
        if (values.size() < minCount) {
            throw new InvalidInputException("Enter at least " + minCount + " values.");
        }
        if (values.size() > maxCount) {
            throw new InvalidInputException("At most " + maxCount + " values are allowed (you entered " + values.size() + ").");
        }
        return values.stream().mapToInt(Integer::intValue).toArray();
    }
}
