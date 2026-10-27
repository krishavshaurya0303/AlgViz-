package com.algoviz.engine;

import com.algoviz.engine.sorting.BubbleSort;
import com.algoviz.engine.sorting.SortInput;
import com.algoviz.engine.sorting.SortOrder;
import com.algoviz.engine.sorting.SortStep;
import com.algoviz.model.ExecutionMetrics;
import com.algoviz.model.MetricType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlaybackControllerTest {

    private final AtomicLong clock = new AtomicLong();
    private PlaybackController<SortStep> pc;
    private ExecutionTrace<SortStep> trace;
    private final List<PlaybackState> events = new ArrayList<>();

    @BeforeEach
    void setUp() {
        pc = new PlaybackController<>(new Stopwatch(clock::get));
        trace = new BubbleSort().run(new SortInput(new int[]{4, 3, 2, 1}, SortOrder.ASCENDING));
        pc.addListener((step, index, state) -> events.add(state));
        pc.load(trace);
    }

    @Test
    void loadStartsReadyAtInitialState() {
        assertEquals(PlaybackState.READY, pc.state());
        assertEquals(0, pc.index());
        assertSame(trace.first(), pc.currentStep());
    }

    @Test
    void pausePreventsFurtherPlayback() {
        pc.play();
        pc.tick();
        pc.tick();
        pc.pause();
        int at = pc.index();
        assertFalse(pc.tick());
        assertFalse(pc.tick());
        assertEquals(at, pc.index());
        assertEquals(PlaybackState.PAUSED, pc.state());
    }

    @Test
    void resumeContinuesFromSameStep() {
        pc.play();
        pc.tick();
        pc.pause();
        int at = pc.index();
        pc.play();
        pc.tick();
        assertEquals(at + 1, pc.index());
    }

    @Test
    void playsToTheEndAndFinishes() {
        pc.play();
        int guard = 0;
        while (pc.tick()) {
            guard++;
            assertTrue(guard < 10_000);
        }
        assertEquals(PlaybackState.FINISHED, pc.state());
        assertEquals(trace.size() - 1, pc.index());
        assertArrayEquals(new int[]{1, 2, 3, 4}, pc.currentStep().values());
    }

    @Test
    void playAfterFinishReplaysFromStart() {
        pc.skipToEnd();
        pc.play();
        assertEquals(0, pc.index());
        assertEquals(PlaybackState.PLAYING, pc.state());
    }

    @Test
    void stepForwardAndBackwardRestoreExactStates() {
        pc.stepForward();
        pc.stepForward();
        SortStep two = pc.currentStep();
        pc.stepForward();
        pc.stepBackward();
        assertSame(two, pc.currentStep());
        assertArrayEquals(trace.get(2).values(), pc.currentStep().values());
        assertEquals(trace.get(2).metrics(), pc.currentStep().metrics());
    }

    @Test
    void repeatedForwardBackwardIsConsistent() {
        List<ExecutionMetrics> forward = new ArrayList<>();
        while (pc.canStepForward()) {
            pc.stepForward();
            forward.add(pc.currentStep().metrics());
        }
        for (int round = 0; round < 3; round++) {
            for (int i = forward.size() - 1; i > 0; i--) {
                pc.stepBackward();
                assertEquals(forward.get(i - 1), pc.currentStep().metrics());
            }
            pc.stepBackward();
            assertEquals(0, pc.index());
            assertEquals(0, pc.currentStep().metrics().get(MetricType.COMPARISONS));
            for (ExecutionMetrics m : forward) {
                pc.stepForward();
                assertEquals(m, pc.currentStep().metrics());
            }
        }
        assertEquals(PlaybackState.FINISHED, pc.state());
    }

    @Test
    void stepBackwardAtStartDoesNothing() {
        assertFalse(pc.stepBackward());
        assertEquals(0, pc.index());
        assertEquals(PlaybackState.READY, pc.state());
    }

    @Test
    void steppingStopsAutomaticPlayback() {
        pc.play();
        pc.stepForward();
        assertNotEquals(PlaybackState.PLAYING, pc.state());
        assertFalse(pc.tick());
    }

    @Test
    void resetRestoresInitialStateAndClearsTime() {
        pc.play();
        clock.addAndGet(5_000_000_000L);
        pc.tick();
        pc.tick();
        pc.reset();
        assertEquals(0, pc.index());
        assertEquals(PlaybackState.READY, pc.state());
        assertEquals(0, pc.elapsedNanos());
        assertArrayEquals(new int[]{4, 3, 2, 1}, pc.currentStep().values());
    }

    @Test
    void elapsedTimeExcludesPausedTime() {
        pc.play();
        clock.addAndGet(2_000);
        pc.pause();
        clock.addAndGet(1_000_000);
        assertEquals(2_000, pc.elapsedNanos());
        pc.play();
        clock.addAndGet(500);
        assertEquals(2_500, pc.elapsedNanos());
    }

    @Test
    void loadingNewTraceInvalidatesGeneration() {
        long g = pc.generation();
        pc.load(trace);
        assertNotEquals(g, pc.generation());
        long g2 = pc.generation();
        pc.clear();
        assertNotEquals(g2, pc.generation());
        assertNull(pc.currentStep());
        assertEquals(PlaybackState.EMPTY, pc.state());
        assertFalse(pc.tick());
    }

    @Test
    void seekJumpsAndUpdatesState() {
        pc.seek(3);
        assertEquals(3, pc.index());
        assertEquals(PlaybackState.PAUSED, pc.state());
        pc.seek(trace.size() - 1);
        assertEquals(PlaybackState.FINISHED, pc.state());
        pc.seek(0);
        assertEquals(PlaybackState.READY, pc.state());
    }

    @Test
    void listenersAreNotified() {
        events.clear();
        pc.play();
        pc.pause();
        pc.stepBackward();
        assertEquals(List.of(PlaybackState.PLAYING, PlaybackState.PAUSED, PlaybackState.READY), events);
    }

    @Test
    void singleStepTraceIsImmediatelyFinished() {
        ExecutionTrace<SortStep> one = new ExecutionTrace<>("x", List.of(trace.first()), 0);
        pc.load(one);
        assertEquals(PlaybackState.FINISHED, pc.state());
        pc.play();
        assertEquals(PlaybackState.FINISHED, pc.state());
    }
}
