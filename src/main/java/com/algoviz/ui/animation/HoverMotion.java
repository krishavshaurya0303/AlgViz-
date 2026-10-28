package com.algoviz.ui.animation;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.util.function.BooleanSupplier;

/**
 * Hover and press motion for the whole application.
 * <p>
 * JavaFX 21 CSS has no transitions, so colours still change instantly through
 * {@code :hover} rules while this class eases position and scale: buttons lift,
 * sidebar items and library rows slide right, dashboard cards rise, heap blocks in
 * the memory view magnify slightly, and buttons
 * press in while clicked. One event filter on the scene covers every screen,
 * including modules that are loaded later. All of it is off when Settings →
 * Animate transitions is off.
 */
public final class HoverMotion {

    private record Motion(double dx, double dy, double scale) {
    }

    private static final Motion NONE = new Motion(0, 0, 1);
    private static final Motion BUTTON = new Motion(0, -1.5, 1);
    private static final Motion SLIDE = new Motion(4, 0, 1);
    private static final Motion ROW = new Motion(3, 0, 1);
    private static final Motion CARD = new Motion(0, -3, 1);
    /** Heap blocks in the memory view magnify slightly, like nodes on the board. */
    private static final Motion BLOCK = new Motion(0, 0, 1.05);
    private static final double PRESSED_SCALE = 0.96;
    private static final Duration HOVER_IN = Duration.millis(150);
    private static final Duration HOVER_OUT = Duration.millis(200);
    private static final Duration PRESS = Duration.millis(100);
    private static final Duration RELEASE = Duration.millis(160);
    private static final String KEY = "algoviz.hover-motion";

    private HoverMotion() {
    }

    /** Installs the motion on {@code scene}; {@code enabled} is checked on every event. */
    public static void install(Scene scene, BooleanSupplier enabled) {
        scene.addEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, e -> {
            if (e.getTarget() instanceof Node n && enabled.getAsBoolean()) {
                Motion m = motionFor(n);
                if (m != null) {
                    animate(n, m, HOVER_IN);
                }
            }
        });
        scene.addEventFilter(MouseEvent.MOUSE_EXITED_TARGET, e -> {
            if (e.getTarget() instanceof Node n && motionFor(n) != null) {
                animate(n, NONE, HOVER_OUT);
            }
        });
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            Node b = buttonAncestor(e.getTarget());
            if (b != null && enabled.getAsBoolean()) {
                animate(b, new Motion(b.getTranslateX(), b.getTranslateY(), PRESSED_SCALE), PRESS);
            }
        });
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            Node b = buttonAncestor(e.getTarget());
            if (b != null) {
                animate(b, b.isHover() && enabled.getAsBoolean() ? BUTTON : NONE, RELEASE);
            }
        });
    }

    private static Motion motionFor(Node n) {
        var classes = n.getStyleClass();
        if (classes.contains("nav-item")) {
            return SLIDE;
        }
        if (classes.contains("module-card")) {
            return CARD;
        }
        if (classes.contains("memory-block")) {
            return BLOCK;
        }
        if (classes.contains("library-item")) {
            return ROW;
        }
        if (classes.contains("button") || classes.contains("toggle-button")) {
            return BUTTON;
        }
        return null;
    }

    private static Node buttonAncestor(Object target) {
        for (Node n = target instanceof Node t ? t : null; n != null; n = n.getParent()) {
            if (n.getStyleClass().contains("button") || n.getStyleClass().contains("toggle-button")) {
                return n.getStyleClass().contains("nav-item") ? null : n;
            }
        }
        return null;
    }

    private static void animate(Node n, Motion to, Duration duration) {
        if (n.getProperties().get(KEY) instanceof Timeline running) {
            running.stop(); // retarget from wherever it is now
        }
        Timeline t = new Timeline(new KeyFrame(duration,
                new KeyValue(n.translateXProperty(), to.dx(), Easing.OUT),
                new KeyValue(n.translateYProperty(), to.dy(), Easing.OUT),
                new KeyValue(n.scaleXProperty(), to.scale(), Easing.OUT),
                new KeyValue(n.scaleYProperty(), to.scale(), Easing.OUT)));
        n.getProperties().put(KEY, t);
        t.play();
    }
}
