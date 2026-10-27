package com.algoviz.ui.animation;

import com.algoviz.config.AppConfig;
import com.algoviz.engine.ExecutionStep;
import com.algoviz.engine.ExecutionTrace;
import com.algoviz.engine.PlaybackController;
import com.algoviz.engine.PlaybackState;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * JavaFX adapter around the UI-independent {@link PlaybackController}.
 * <p>
 * A {@link Timeline} running on the JavaFX Application Thread calls
 * {@code tick()} at the selected speed, so all state changes and all rendering
 * happen on one thread — there are no races between play, pause, step and reset.
 * Changing the speed only changes the timeline's rate; the current step is untouched.
 * The controller's state is mirrored into observable properties for bindings.
 */
public final class FxPlaybackDriver<S extends ExecutionStep> {

    private final PlaybackController<S> controller = new PlaybackController<>();
    private final Timeline ticker;
    private final Timeline clockRefresh;
    private final DoubleProperty speed = new SimpleDoubleProperty(AppConfig.SPEED_DEFAULT);
    private final ReadOnlyObjectWrapper<PlaybackState> state = new ReadOnlyObjectWrapper<>(PlaybackState.EMPTY);
    private final ReadOnlyIntegerWrapper index = new ReadOnlyIntegerWrapper();
    private final ReadOnlyIntegerWrapper size = new ReadOnlyIntegerWrapper();
    private final ReadOnlyObjectWrapper<S> step = new ReadOnlyObjectWrapper<>();
    private final LongProperty elapsedNanos = new SimpleLongProperty();
    private final List<StepChangeListener<S>> changeListeners = new ArrayList<>();
    private S previousStep;

    public FxPlaybackDriver() {
        // One tick per second at rate 1; the rate equals steps per second.
        ticker = new Timeline(new KeyFrame(Duration.seconds(1), e -> onTick()));
        ticker.setCycleCount(Animation.INDEFINITE);
        // Not bound: Timeline.playFromStart() writes the rate itself.
        ticker.setRate(speed.get());
        speed.addListener((o, a, v) -> ticker.setRate(v.doubleValue()));

        clockRefresh = new Timeline(new KeyFrame(Duration.millis(100),
                e -> elapsedNanos.set(controller.elapsedNanos())));
        clockRefresh.setCycleCount(Animation.INDEFINITE);

        controller.addListener((s, i, st) -> {
            S before = step.get();
            if (before != s) {
                previousStep = before;
            }
            index.set(i);
            size.set(controller.size());
            elapsedNanos.set(controller.elapsedNanos());
            step.set(s);
            state.set(st);
            if (st == PlaybackState.PLAYING) {
                if (ticker.getStatus() != Animation.Status.RUNNING) {
                    ticker.playFromStart();
                    clockRefresh.play();
                }
            } else {
                ticker.stop();
                clockRefresh.stop();
            }
            for (StepChangeListener<S> l : changeListeners) {
                l.changed(before, s, st);
            }
        });
    }

    private void onTick() {
        // Guard against a tick that was already queued when playback stopped.
        if (!controller.isPlaying()) {
            ticker.stop();
            return;
        }
        controller.tick();
    }

    // ---------------------------------------------------------------- commands

    public void load(ExecutionTrace<S> trace) {
        previousStep = null;
        controller.load(trace);
    }

    public void loadAndPlay(ExecutionTrace<S> trace) {
        load(trace);
        controller.play();
    }

    public void loadAtEnd(ExecutionTrace<S> trace) {
        previousStep = null;
        controller.loadAtEnd(trace);
    }

    public void clear() {
        controller.clear();
    }

    public void play() {
        controller.play();
    }

    public void pause() {
        controller.pause();
    }

    public void togglePlayPause() {
        controller.togglePlayPause();
    }

    public void stepForward() {
        controller.stepForward();
    }

    public void stepBackward() {
        controller.stepBackward();
    }

    public void reset() {
        controller.reset();
    }

    public void skipToEnd() {
        controller.skipToEnd();
    }

    public void seek(int i) {
        controller.seek(i);
    }

    /** Stops timers; used when the module is hidden so no stale updates can occur. */
    public void suspend() {
        controller.pause();
        ticker.stop();
        clockRefresh.stop();
    }

    // ---------------------------------------------------------------- queries

    public PlaybackController<S> controller() {
        return controller;
    }

    public DoubleProperty speedProperty() {
        return speed;
    }

    public double speed() {
        return speed.get();
    }

    /** Suggested duration of the visual transition between two steps. */
    public double transitionMillis() {
        return Math.min(320, 0.75 * 1000 / Math.max(0.5, speed.get()));
    }

    public ReadOnlyObjectProperty<PlaybackState> stateProperty() {
        return state.getReadOnlyProperty();
    }

    public PlaybackState state() {
        return state.get();
    }

    public ReadOnlyIntegerProperty indexProperty() {
        return index.getReadOnlyProperty();
    }

    public ReadOnlyIntegerProperty sizeProperty() {
        return size.getReadOnlyProperty();
    }

    public ReadOnlyObjectProperty<S> stepProperty() {
        return step.getReadOnlyProperty();
    }

    public S currentStep() {
        return step.get();
    }

    /** The step displayed before the current one (null after a load). */
    public S previousStep() {
        return previousStep;
    }

    public ReadOnlyLongProperty elapsedProperty() {
        return elapsedNanos;
    }

    public boolean hasTrace() {
        return controller.hasTrace();
    }

    public boolean isPlaying() {
        return controller.isPlaying();
    }

    public ExecutionTrace<S> trace() {
        return controller.trace();
    }

    /**
     * Registers a callback invoked after every playback change with the previously
     * displayed step and the new one (identical objects when only the state changed).
     */
    public void addChangeListener(StepChangeListener<S> listener) {
        changeListeners.add(listener);
    }

    /** Callback for renderers. */
    @FunctionalInterface
    public interface StepChangeListener<S> {
        void changed(S previous, S current, PlaybackState state);
    }
}
