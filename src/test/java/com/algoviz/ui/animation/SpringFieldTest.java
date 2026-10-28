package com.algoviz.ui.animation;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringFieldTest {

    /** A chain 1 - 2 - 3. */
    private static SpringField chain() {
        SpringField f = new SpringField(null);
        f.setLinks(Set.of(1, 2, 3), List.of(new int[]{1, 2}, new int[]{2, 3}));
        return f;
    }

    private static void run(SpringField f, double seconds) {
        for (int i = 0; i < seconds * 60; i++) {
            f.step(1.0 / 60);
        }
    }

    @Test
    void grabbedNodeStaysUnderTheCursor() {
        SpringField f = chain();
        f.grab(1, 80, -40);
        run(f, 1);
        assertEquals(80, f.dx(1), 1e-9);
        assertEquals(-40, f.dy(1), 1e-9);
    }

    @Test
    void neighboursFollowLessTheFartherAwayTheyAre() {
        SpringField f = chain();
        f.grab(1, 100, 0);
        run(f, 2);
        assertTrue(f.dx(2) > 0, "direct neighbour is pulled along");
        assertTrue(f.dx(3) > 0, "second neighbour is pulled along too");
        assertTrue(f.dx(2) < 100 && f.dx(3) < f.dx(2), "the pull weakens along the chain");
    }

    @Test
    void everythingReturnsHomeAfterRelease() {
        SpringField f = chain();
        f.grab(2, 60, 60);
        run(f, 1);
        f.release();
        run(f, 4);
        assertTrue(f.isSettled());
        for (int id : new int[]{1, 2, 3}) {
            assertEquals(0, f.dx(id), 1e-9);
            assertEquals(0, f.dy(id), 1e-9);
        }
    }

    @Test
    void releasedNodeKeepsTheSpeedItWasThrownWith() throws InterruptedException {
        SpringField f = chain();
        f.grab(1, 0, 0);
        for (int x = 20; x <= 60; x += 20) {
            Thread.sleep(10);
            f.drag(x, 0);
        }
        f.release();
        f.step(1.0 / 120);
        assertTrue(f.dx(1) > 60, "a node thrown to the right keeps moving right for a moment: " + f.dx(1));
    }

    @Test
    void removedNodesAreForgotten() {
        SpringField f = chain();
        f.grab(3, 50, 0);
        f.setLinks(Set.of(1, 2), List.of(new int[]{1, 2}));
        assertFalse(f.isGrabbing());
        assertEquals(0, f.dx(3), 1e-9);
    }
}
