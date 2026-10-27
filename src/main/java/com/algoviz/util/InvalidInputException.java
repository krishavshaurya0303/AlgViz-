package com.algoviz.util;

/**
 * Checked exception for user input that cannot be accepted. The message is
 * written for the end user and is shown inline next to the input field.
 */
public class InvalidInputException extends Exception {

    public InvalidInputException(String message) {
        super(message);
    }
}
