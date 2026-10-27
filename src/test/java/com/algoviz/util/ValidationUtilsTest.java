package com.algoviz.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationUtilsTest {

    @Test
    void parsesMixedSeparators() throws Exception {
        assertArrayEquals(new int[]{5, -3, 8, 1}, ValidationUtils.parseIntList(" 5, -3 8;1 ", 1, 10, -10, 10));
    }

    @Test
    void rejectsBadLists() {
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseIntList("", 1, 10, 0, 9));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseIntList("1, x", 1, 10, 0, 9));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseIntList("1, 99", 1, 10, 0, 9));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseIntList("1", 2, 10, 0, 9));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseIntList("1 2 3", 1, 2, 0, 9));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseIntList("1.5", 1, 2, 0, 9));
    }

    @Test
    void parsesSingleValue() throws Exception {
        assertEquals(42, ValidationUtils.parseInt(" 42 ", "value", 0, 100));
        InvalidInputException e = assertThrows(InvalidInputException.class,
                () -> ValidationUtils.parseInt("101", "value", 0, 100));
        assertEquals("Value must be between 0 and 100.", e.getMessage());
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseInt(" ", "value", 0, 100));
        assertThrows(InvalidInputException.class, () -> ValidationUtils.parseInt("99999999999", "value", 0, 100));
    }
}
