package com.algoviz.ui.component;

import com.algoviz.engine.ExecutionStep;
import com.algoviz.ui.animation.FxPlaybackDriver;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

import java.util.Locale;

/**
 * Shared execution-control toolbar: step backward, step forward and the
 * current step number. The user walks through a run one step at a time;
 * there is no automatic playback. Buttons enable/disable themselves from
 * the driver's position.
 */
public final class PlaybackBar extends HBox {

    private final Button back = stepButton("Previous", IconType.STEP_BACK, "Previous step (←)");
    private final Button forward = stepButton("Next", IconType.STEP_FORWARD, "Next step (→)");
    private final Label stepLabel = new Label("No run yet");

    public PlaybackBar() {
        getStyleClass().add("playback-bar");
        setAlignment(Pos.CENTER_LEFT);
        back.getStyleClass().add("step-back-button");
        forward.getStyleClass().remove("ghost");
        forward.getStyleClass().addAll("primary", "step-forward-button");
        forward.setContentDisplay(ContentDisplay.RIGHT);
        stepLabel.getStyleClass().add("step-label");
        getChildren().addAll(back, forward, stepLabel);
        setDisable(true);
    }

    public <S extends ExecutionStep> void bind(FxPlaybackDriver<S> driver) {
        back.setOnAction(e -> driver.stepBackward());
        forward.setOnAction(e -> driver.stepForward());
        driver.addChangeListener((prev, cur, st) -> refresh(driver));
        refresh(driver);
    }

    private <S extends ExecutionStep> void refresh(FxPlaybackDriver<S> driver) {
        boolean has = driver.hasTrace();
        setDisable(false);
        int idx = driver.indexProperty().get();
        int size = driver.sizeProperty().get();
        back.setDisable(!has || idx == 0);
        forward.setDisable(!has || idx >= size - 1);
        stepLabel.setText(has && size > 1 ? positionText(idx, size) : "No steps to replay");
    }

    /**
     * Snapshot 0 is the untouched input and every later snapshot is one recorded step,
     * so a trace of {@code size} snapshots holds {@code size - 1} steps.
     */
    public static String positionText(int index, int size) {
        int steps = Math.max(0, size - 1);
        return index == 0
                ? String.format(Locale.ROOT, "Start  ·  %d step%s", steps, steps == 1 ? "" : "s")
                : String.format(Locale.ROOT, "Step %d of %d", index, steps);
    }

    private static Button stepButton(String text, IconType type, String tooltip) {
        Button b = new Button(text);
        b.setGraphic(Icons.create(type, 14));
        b.setGraphicTextGap(8);
        b.getStyleClass().add("ghost");
        b.setTooltip(new Tooltip(tooltip));
        b.setAccessibleText(tooltip);
        return b;
    }
}
