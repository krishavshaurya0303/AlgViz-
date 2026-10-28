package com.algoviz.ui.animation;

import javafx.animation.Interpolator;

/**
 * The two easing curves used for all UI motion. JavaFX's built-in EASE_OUT and
 * EASE_BOTH are too gentle: motion that starts weakly feels late. These curves
 * move most of the way at once and then settle.
 */
public final class Easing {

    /** Things responding to the user (press, appear): fast start, soft landing. */
    public static final Interpolator OUT = Interpolator.SPLINE(0.23, 1, 0.32, 1);

    /** Things moving on screen from one place to another (bars swapping, nodes sliding). */
    public static final Interpolator IN_OUT = Interpolator.SPLINE(0.77, 0, 0.175, 1);

    private Easing() {
    }
}
