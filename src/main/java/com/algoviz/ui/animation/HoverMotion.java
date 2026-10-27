package com.algoviz.ui.animation;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.util.function.BooleanSupplier;

/**
 * Small hover and press motions for the whole application.
 * <p>
 * JavaFX 21 CSS has no transitions, so colours still change instantly through
 * {@code :hover} rules while this class eases position and scale: buttons lift,
 * sidebar items and library rows slide right, dashboard cards rise, and buttons
 * press in when clicked. One event filter on the scene covers every screen,
 * including modules that are loaded later.
 */
public final class HoverMotion {

    private record Motion(double dx, double dy) {
    }

    private static final Motion BUTTON = new Motion(0, -1.5);
    private static final Motion SLIDE = new Motion(4, 0);
    private static final Motion ROW = new Motion(3, 0);
    private static final Motion CARD = new Motion(0, -3);
    private static final double PRESSED_SCALE = 0.97;
    private static final Duration IN = Duration.millis(140);
    private static final Duration OUT = Duration.millis(200);
    private static final String KEY = "algoviz.hover-motion";

    private HoverMotion() {
    }

    /** Installs the motions on {@code scene}; {@code enabled} is checked on every event (Settings → Animate transitions). */
    public static void install(Scene scene, BooleanSupplier enabled) {
        scene.addEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, e -> {
            if (e.getTarget() instanceof Node n && enabled.getAsBoolean()) {
                Motion m = motionFor(n);
                if (m != null) {
                    animate(n, m.dx(), m.dy(), 1, IN);
                }
            }
        });
        scene.addEventFilter(MouseEvent.MOUSE_EXITED_TARGET, e -> {
            if (e.getTarget() instanceof Node n && motionFor(n) != null) {
                animate(n, 0, 0, 1, OUT);
            }
        });
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            Node b = buttonAncestor(e.getTarget());
            if (b != null && enabled.getAsBoolean()) {
                animate(b, b.getTranslateX(), b.getTranslateY(), PRESSED_SCALE, Duration.millis(70));
            }
        });
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            Node b = buttonAncestor(e.getTarget());
            if (b != null) {
                Motion m = b.isHover() && enabled.getAsBoolean() ? BUTTON : new Motion(0, 0);
                animate(b, m.dx(), m.dy(), 1, IN);
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

    private static void animate(Node n, double tx, double ty, double scale, Duration duration) {
        if (n.getProperties().get(KEY) instanceof Timeline running) {
            running.stop();
        }
        Timeline t = new Timeline(new KeyFrame(duration,
                new KeyValue(n.translateXProperty(), tx, Interpolator.EASE_OUT),
                new KeyValue(n.translateYProperty(), ty, Interpolator.EASE_OUT),
                new KeyValue(n.scaleXProperty(), scale, Interpolator.EASE_OUT),
                new KeyValue(n.scaleYProperty(), scale, Interpolator.EASE_OUT)));
        n.getProperties().put(KEY, t);
        t.play();
    }
}
