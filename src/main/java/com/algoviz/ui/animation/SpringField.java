package com.algoviz.ui.animation;

import javafx.animation.AnimationTimer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Elastic, draggable nodes for the tree and linked-list canvases (in the spirit
 * of Obsidian's graph view).
 * <p>
 * Every node keeps a <em>displacement</em> from the position its canvas layout
 * gives it. Three forces act on it: a spring pulling it back home, springs along
 * each link pulling it towards its neighbours' displacement, and damping. A
 * grabbed node is pinned to the cursor, so its neighbours are dragged along like
 * a rope and everything glides back home when it is released. The layout itself
 * never changes, so the picture always returns to the correct structure.
 * <p>
 * {@link #step(double)} is plain arithmetic and can be tested without JavaFX;
 * the animation timer is only created the first time the field moves.
 */
public final class SpringField {

    /** Pull back to the layout position (1/s²). */
    static final double HOME_K = 120;
    /** Pull along a link towards the neighbour's displacement (1/s²). */
    static final double LINK_K = 100;
    /** Velocity damping (1/s); below critical, so nodes overshoot slightly and settle. */
    static final double DAMPING = 10;
    /** Push between two nodes that come closer than their minimum distance (1/s²). */
    static final double REPEL_K = 900;
    private static final double MAX_DT = 1.0 / 240;
    private static final double REST = 0.05;

    private static final class Body {
        double dx;
        double dy;
        double vx;
        double vy;
    }

    private final Runnable onFrame;
    private final Map<Integer, Body> bodies = new HashMap<>();
    private final Map<Integer, List<Integer>> neighbours = new HashMap<>();
    /** Layout positions (used only to keep nodes from piling up while dragged). */
    private final Map<Integer, double[]> homes = new HashMap<>();
    private double minDistance;
    private Integer grabbed;
    private AnimationTimer timer;
    private long lastNanos;

    public SpringField(Runnable onFrame) {
        this.onFrame = onFrame;
    }

    /** Replaces the links (pairs of node ids). Nodes that no longer exist are forgotten. */
    public void setLinks(Set<Integer> ids, List<int[]> links) {
        neighbours.clear();
        for (int[] l : links) {
            neighbours.computeIfAbsent(l[0], k -> new ArrayList<>()).add(l[1]);
            neighbours.computeIfAbsent(l[1], k -> new ArrayList<>()).add(l[0]);
        }
        bodies.keySet().retainAll(new HashSet<>(ids));
        homes.keySet().retainAll(new HashSet<>(ids));
        if (grabbed != null && !ids.contains(grabbed)) {
            grabbed = null;
        }
        wake();
    }

    /** Tells the field where node {@code id} sits in the layout and how close two nodes may get. */
    public void home(int id, double x, double y, double minDistance) {
        homes.put(id, new double[]{x, y});
        this.minDistance = minDistance;
    }

    public double dx(int id) {
        Body b = bodies.get(id);
        return b == null ? 0 : b.dx;
    }

    public double dy(int id) {
        Body b = bodies.get(id);
        return b == null ? 0 : b.dy;
    }

    public boolean isGrabbing() {
        return grabbed != null;
    }

    /** Pins node {@code id} so that it is drawn {@code (dx, dy)} away from its layout position. */
    public void grab(int id, double dx, double dy) {
        grabbed = id;
        drag(dx, dy);
    }

    public void drag(double dx, double dy) {
        if (grabbed == null) {
            return;
        }
        Body b = bodies.computeIfAbsent(grabbed, k -> new Body());
        b.dx = dx;
        b.dy = dy;
        b.vx = 0;
        b.vy = 0;
        wake();
    }

    public void release() {
        grabbed = null;
        wake();
    }

    /** Drops every displacement immediately. */
    public void reset() {
        grabbed = null;
        bodies.clear();
        if (timer != null) {
            timer.stop();
        }
    }

    /** Advances the simulation by {@code seconds}. Returns true while anything is still moving. */
    public boolean step(double seconds) {
        double left = Math.min(seconds, 0.05);
        while (left > 1e-9) {
            double dt = Math.min(MAX_DT, left);
            substep(dt);
            left -= dt;
        }
        return !isSettled();
    }

    private void substep(double dt) {
        Set<Integer> ids = new HashSet<>(bodies.keySet());
        for (Integer id : bodies.keySet()) {
            ids.addAll(neighbours.getOrDefault(id, List.of()));
        }
        if (!bodies.isEmpty()) {
            ids.addAll(homes.keySet());
        }
        Map<Integer, double[]> accel = new HashMap<>();
        for (Integer id : ids) {
            if (id.equals(grabbed)) {
                continue;
            }
            double dx = dx(id);
            double dy = dy(id);
            Body b = bodies.get(id);
            double vx = b == null ? 0 : b.vx;
            double vy = b == null ? 0 : b.vy;
            double ax = -HOME_K * dx - DAMPING * vx;
            double ay = -HOME_K * dy - DAMPING * vy;
            for (Integer n : neighbours.getOrDefault(id, List.of())) {
                ax += LINK_K * (dx(n) - dx);
                ay += LINK_K * (dy(n) - dy);
            }
            double[] h = homes.get(id);
            if (h != null && minDistance > 0) {
                double px = h[0] + dx;
                double py = h[1] + dy;
                for (Map.Entry<Integer, double[]> o : homes.entrySet()) {
                    if (o.getKey().equals(id)) {
                        continue;
                    }
                    double ox = px - (o.getValue()[0] + dx(o.getKey()));
                    double oy = py - (o.getValue()[1] + dy(o.getKey()));
                    double dist = Math.hypot(ox, oy);
                    if (dist < minDistance && dist > 1e-6) {
                        double push = REPEL_K * (minDistance - dist) / dist;
                        ax += push * ox;
                        ay += push * oy;
                    }
                }
            }
            accel.put(id, new double[]{ax, ay});
        }
        accel.forEach((id, a) -> {
            Body b = bodies.computeIfAbsent(id, k -> new Body());
            b.vx += a[0] * dt;
            b.vy += a[1] * dt;
            b.dx += b.vx * dt;
            b.dy += b.vy * dt;
        });
        if (grabbed == null) {
            bodies.values().removeIf(b -> Math.abs(b.dx) < REST && Math.abs(b.dy) < REST
                    && Math.abs(b.vx) < REST && Math.abs(b.vy) < REST);
        }
    }

    public boolean isSettled() {
        return grabbed == null && bodies.isEmpty();
    }

    private void wake() {
        if (onFrame == null) {
            return; // headless use (tests): the caller drives step() itself
        }
        if (timer == null) {
            timer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    double seconds = lastNanos == 0 ? 1.0 / 60 : (now - lastNanos) / 1e9;
                    lastNanos = now;
                    boolean moving = step(seconds);
                    onFrame.run();
                    if (!moving) {
                        stop();
                        lastNanos = 0;
                    }
                }
            };
        }
        timer.start();
    }
}
