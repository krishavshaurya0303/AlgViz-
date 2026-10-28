package com.algoviz.controller;

import com.algoviz.engine.ExecutionStep;
import com.algoviz.engine.ExecutionTrace;
import com.algoviz.engine.PlaybackState;
import com.algoviz.engine.memory.MemoryLayout;
import com.algoviz.engine.memory.MemoryModel;
import com.algoviz.model.StepStatus;
import com.algoviz.ui.animation.BackgroundRunner;
import com.algoviz.ui.animation.FxPlaybackDriver;
import com.algoviz.ui.component.ComplexityCard;
import com.algoviz.ui.component.ExplanationCard;
import com.algoviz.ui.component.Legend;
import com.algoviz.ui.component.MemoryView;
import com.algoviz.ui.component.PlaybackBar;
import com.algoviz.ui.component.PseudocodePanel;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;

import java.util.Locale;

/**
 * Common base of the four visualizer screens (Sorting, Graph, Tree, Linked List).
 * <p>
 * It owns the playback driver and wires the shared components — playback bar,
 * explanation card and pseudocode — so each subclass only provides its
 * input controls and its {@link #render} method. The algorithm engines never
 * see this class: they produce traces, this class renders them.
 *
 * @param <S> step type of the module
 */
public abstract class VisualizerController<S extends ExecutionStep> implements ModuleController {

    protected final AppContext context;
    protected final FxPlaybackDriver<S> driver = new FxPlaybackDriver<>();
    protected final BackgroundRunner runner = new BackgroundRunner();
    private final ReadOnlyStringWrapper status = new ReadOnlyStringWrapper("Idle");
    private final ReadOnlyObjectWrapper<StepStatus> tone = new ReadOnlyObjectWrapper<>(StepStatus.INFO);

    @FXML protected PlaybackBar playbackBar;
    @FXML protected ExplanationCard explanationCard;
    @FXML protected PseudocodePanel pseudocodePanel;
    @FXML protected ComplexityCard complexityCard;
    @FXML protected Legend legend;
    /** Optional: present only on screens whose data structure lives in dynamic memory. */
    @FXML protected MemoryView memoryView;

    protected VisualizerController(AppContext context) {
        this.context = context;
    }

    /** Subclasses call this from their initialize() after configuring their own components. */
    protected final void initPlayback() {
        playbackBar.bind(driver);
        driver.addChangeListener(this::onPlaybackChanged);
        if (memoryView != null) {
            memoryView.modelProperty().set(MemoryModel.parse(context.settings().memoryModel()));
            memoryView.modelProperty().addListener((o, a, v) -> {
                context.settings().setMemoryModel(v.name());
                context.saveSettings();
                refreshMemory();
            });
            memoryView.expandedProperty().set(context.settings().showMemory());
            memoryView.expandedProperty().addListener((o, a, v) -> {
                context.settings().setShowMemory(v);
                context.saveSettings();
            });
        }
    }

    /** Rebuilds the memory view for the step on screen. */
    private void refreshMemory() {
        if (memoryView == null) {
            return;
        }
        ExecutionTrace<S> trace = driver.controller().trace();
        memoryView.show(trace == null ? null : memoryLayout(trace, driver.indexProperty().get(), memoryView.model()));
    }

    /**
     * Memory picture of step {@code index} of {@code trace}; screens with a memory view override this.
     * The whole trace is passed so the layout can compare with the step before and remember
     * nodes that disappeared earlier in the operation.
     */
    protected MemoryLayout memoryLayout(ExecutionTrace<S> trace, int index, MemoryModel model) {
        return null;
    }

    private void onPlaybackChanged(S previous, S current, PlaybackState state) {
        if (current != null) {
            explanationCard.show(current.explanation(), current.status());
            pseudocodePanel.highlight(current.pseudocodeLine());
        }
        if (previous != current) {
            render(previous, current, transitionMillis());
        }
        refreshMemory();
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
        if (driver.hasTrace() && driver.sizeProperty().get() > 1) {
            text += "  ·  " + PlaybackBar.positionText(driver.indexProperty().get(), driver.sizeProperty().get()).toLowerCase(Locale.ROOT);
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
    public void applySettings() {
        if (memoryView != null) {
            memoryView.modelProperty().set(MemoryModel.parse(context.settings().memoryModel()));
            memoryView.expandedProperty().set(context.settings().showMemory());
        }
    }

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
