package com.algoviz;

/**
 * Plain main class for the self-contained jar. Launching a class that does not
 * extend Application lets JavaFX start from the classpath without module flags.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        AlgoVizApplication.main(args);
    }
}
