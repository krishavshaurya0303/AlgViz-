package com.algoviz.controller;

import com.algoviz.engine.ExecutionStep;
import com.algoviz.engine.PlaybackState;
import com.algoviz.model.StepStatus;
import com.algoviz.ui.animation.BackgroundRunner;
import com.algoviz.ui.animation.FxPlaybackDriver;
import com.algoviz.ui.component.ComplexityCard;
import com.algoviz.ui.component.ExplanationCard;
import com.algoviz.ui.component.Legend;
import com.algoviz.ui.component.MetricsPanel;
import com.algoviz.ui.component.PlaybackBar;
import com.algoviz.ui.component.PseudocodePanel;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;

/**
 * Common base of the four visualizer screens (Sorting, Graph, Tree, Linked List).
 * <p>
 * It owns the playback driver and wires the shared components — playback bar,
 * explanation card, pseudocode, metrics — so each subclass only provides its
 * input controls and its {@link #render} method. The algorithm engines never
 * see this class: they produce traces, this class renders them.
 *
 * @param <S> step type of the module
 */
public abstract class VisualizerController<S extends ExecutionStep> implements ModuleController {

    protected static final String STEP = "Step";

    protected final AppContext context;
    protected final FxPlaybackDriver<S> driver = new FxPlaybackDriver<>();
    protected final BackgroundRunner runner = new BackgroundRunner();
    private final ReadOnlyStringWrapper status = new ReadOnlyStringWrapper("Idle");
    private final ReadOnlyObjectWrapper<StepStatus> tone = new ReadOnlyObjectWrapper<>(StepStatus.INFO);

    @FXML protected PlaybackBar playbackBar;
    @FXML protected ExplanationCard explanationCard;
    @FXML protected PseudocodePanel pseudocodePanel;
    @FXML protected MetricsPanel metricsPanel;
    @FXML protected ComplexityCard complexityCard;
    @FXML protected Legend legend;

    protected VisualizerController(AppContext context) {
        this.context = context;
    }

    /** Subclasses call this from their initialize() after configuring their own components. */
    protected final void initPlayback() {
        playbackBar.bind(driver);
        driver.addChangeListener(this::onPlaybackChanged);
    }

    private void onPlaybackChanged(S previous, S current, PlaybackState state) {
        if (current != null) {
            explanationCard.show(current.explanation(), current.status());
            pseudocodePanel.highlight(current.pseudocodeLine());
            metricsPanel.show(current.metrics());
            metricsPanel.setExtra(STEP, driver.indexProperty().get() + " / " + Math.max(0, driver.sizeProperty().get() - 1));
        } else {
            metricsPanel.show(null);
            metricsPanel.setExtra(STEP, "—");
        }
        if (previous != current) {
            render(previous, current, transitionMillis());
        }
        updateStatus(state, current);
    }

    /** Draws a step. {@code previous} is the step shown before (for transitions) and may be null. */
    protected abstract void render(S previous, S current, double millis);

    protected double transitionMillis() {
        return context.settings().animateTransitions() ? driver.transitionMillis() : 0;
    }

    private void updateStatus(PlaybackState state, S current) {
        // Mid-run positions are PAUSED in the engine; with manual stepping only, call that "Stepping".
        String text = state == PlaybackState.PAUSED ? "Stepping" : state.label();
        if (driver.hasTrace()) {
            text += "  ·  step " + driver.indexProperty().get() + " / " + Math.max(0, driver.sizeProperty().get() - 1);
        }
        status.set(text);
        StepStatus t = switch (state) {
            case PLAYING, PAUSED -> StepStatus.ACTIVE;
            case FINISHED -> current != null && current.status() == StepStatus.FAILURE
                    ? StepStatus.FAILURE : StepStatus.SUCCESS;
            default -> StepStatus.INFO;
        };
        tone.set(t);
    }

    // ------------------------------------------------------- ModuleController

    @Override
    public void onHide() {
        driver.suspend();
    }

    @Override
    public ReadOnlyStringProperty statusText() {
        return status.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyObjectProperty<StepStatus> statusTone() {
        return tone.getReadOnlyProperty();
    }

    @Override
    public boolean handleKey(KeyEvent e) {
        switch (e.getCode()) {
            case RIGHT -> driver.stepForward();
            case LEFT -> driver.stepBackward();
            default -> {
                return false;
            }
        }
        return true;
    }
}
