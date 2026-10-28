package com.algoviz.ui.animation;

import javafx.animation.AnimationTimer;

/**
 * Drives a 0 → 1 progress value over a duration with ease-in-out, calling a
 * redraw callback every frame. Starting a new tween cancels the previous one,
 * so a fast sequence of steps never leaves an old animation running.
 */
public final class Tween {

    private final Runnable onFrame;
    private final AnimationTimer timer;
    private long startNanos;
    private double durationNanos;
    private double progress = 1;

    public Tween(Runnable onFrame) {
        this.onFrame = onFrame;
        this.timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (startNanos == 0) {
                    startNanos = now;
                }
                double t = durationNanos <= 0 ? 1 : (now - startNanos) / durationNanos;
                progress = Math.min(1, t);
                onFrame.run();
                if (progress >= 1) {
                    stop();
                }
            }
        };
    }

    /** Starts a new transition. A duration of 0 jumps straight to the end. */
    public void start(double millis) {
        timer.stop();
        if (millis <= 1) {
            progress = 1;
            onFrame.run();
            return;
        }
        durationNanos = millis * 1_000_000;
        startNanos = 0;
        progress = 0;
        timer.start();
    }

    public void finish() {
        timer.stop();
        progress = 1;
    }

    /** Eased progress in [0, 1] (strong ease-in-out: things moving across the screen). */
    public double eased() {
        return Easing.IN_OUT.interpolate(0.0, 1.0, progress);
    }

    public boolean isRunning() {
        return progress < 1;
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
