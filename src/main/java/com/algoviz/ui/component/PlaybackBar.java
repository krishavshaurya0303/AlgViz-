package com.algoviz.ui.component;

import com.algoviz.config.AppConfig;
import com.algoviz.engine.ExecutionStep;
import com.algoviz.engine.PlaybackState;
import com.algoviz.ui.animation.FxPlaybackDriver;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.Locale;

/**
 * Shared execution-control toolbar: step back, play/pause, step forward,
 * skip to end, reset, a progress (seek) slider and a speed slider.
 * Buttons enable/disable themselves from the driver's state.
 */
public final class PlaybackBar extends HBox {

    private final Button back = iconButton(IconType.STEP_BACK, "Step backward (←)");
    private final Button play = new Button();
    private final Button forward = iconButton(IconType.STEP_FORWARD, "Step forward (→)");
    private final Button end = iconButton(IconType.SKIP_END, "Jump to the final step (End)");
    private final Button reset = iconButton(IconType.RESET, "Reset to the initial state (R)");
    private final Slider progress = new Slider(0, 1, 0);
    private final Label stepLabel = new Label("Step 0 / 0");
    private final Slider speed = new Slider(0, 1, 0.5);
    private final Label speedLabel = new Label();
    private final Node playIcon = Icons.create(IconType.PLAY, 18);
    private final Node pauseIcon = Icons.create(IconType.PAUSE, 18);
    private boolean updatingProgress;
    private Runnable onBeforePlay;

    public PlaybackBar() {
        getStyleClass().add("playback-bar");
        setAlignment(Pos.CENTER_LEFT);

        play.getStyleClass().add("play-button");
        play.setGraphic(playIcon);
        play.setTooltip(new Tooltip("Play / pause (Space)"));
        play.setAccessibleText("Play or pause");

        progress.setFocusTraversable(true);
        progress.setTooltip(new Tooltip("Drag to jump to any recorded step"));
        HBox.setHgrow(progress, Priority.ALWAYS);
        progress.setMaxWidth(Double.MAX_VALUE);
        stepLabel.getStyleClass().add("step-label");

        Label speedTitle = new Label("Speed");
        speedTitle.getStyleClass().add("field-label");
        speed.setPrefWidth(110);
        speed.setTooltip(new Tooltip("Steps per second during playback"));
        speedLabel.getStyleClass().add("speed-label");

        Region sep1 = separator();
        Region sep2 = separator();
        getChildren().addAll(back, play, forward, end, reset, sep1, progress, stepLabel, sep2,
                speedTitle, speed, speedLabel);
        setDisable(true);
    }

    /** Runs before Play starts, e.g. to build the trace lazily. */
    public void setOnBeforePlay(Runnable r) {
        this.onBeforePlay = r;
    }

    public <S extends ExecutionStep> void bind(FxPlaybackDriver<S> driver) {
        back.setOnAction(e -> driver.stepBackward());
        forward.setOnAction(e -> driver.stepForward());
        end.setOnAction(e -> driver.skipToEnd());
        reset.setOnAction(e -> driver.reset());
        play.setOnAction(e -> {
            if (!driver.isPlaying() && onBeforePlay != null) {
                onBeforePlay.run();
            }
            driver.togglePlayPause();
        });

        // Speed slider uses a logarithmic scale so that slow speeds have fine control.
        speed.setValue(toSlider(driver.speed()));
        speed.valueProperty().addListener((o, a, v) -> driver.speedProperty().set(fromSlider(v.doubleValue())));
        driver.speedProperty().addListener((o, a, v) -> {
            if (Math.abs(fromSlider(speed.getValue()) - v.doubleValue()) > 1e-6) {
                speed.setValue(toSlider(v.doubleValue()));
            }
            updateSpeedLabel(v.doubleValue());
        });
        updateSpeedLabel(driver.speed());

        progress.valueProperty().addListener((o, a, v) -> {
            if (!updatingProgress && driver.hasTrace()) {
                int target = (int) Math.round(v.doubleValue());
                if (target != driver.indexProperty().get()) {
                    driver.seek(target);
                }
            }
        });

        driver.addChangeListener((prev, cur, st) -> refresh(driver, st));
        refresh(driver, driver.state());
    }

    private <S extends ExecutionStep> void refresh(FxPlaybackDriver<S> driver, PlaybackState st) {
        boolean has = driver.hasTrace();
        setDisable(false);
        int idx = driver.indexProperty().get();
        int size = driver.sizeProperty().get();
        back.setDisable(!has || idx == 0);
        forward.setDisable(!has || idx >= size - 1);
        end.setDisable(!has || idx >= size - 1);
        reset.setDisable(!has || (idx == 0 && st != PlaybackState.PLAYING));
        play.setDisable(!has || size <= 1);
        progress.setDisable(!has || size <= 1);
        boolean playing = st == PlaybackState.PLAYING;
        play.setGraphic(playing ? pauseIcon : playIcon);
        play.getTooltip().setText(playing ? "Pause (Space)"
                : st == PlaybackState.FINISHED ? "Replay from the start (Space)"
                : st == PlaybackState.PAUSED ? "Resume (Space)" : "Play (Space)");

        updatingProgress = true;
        progress.setMax(Math.max(1, size - 1));
        progress.setValue(idx);
        updatingProgress = false;
        stepLabel.setText(has ? String.format(Locale.ROOT, "Step %d / %d", idx, Math.max(0, size - 1)) : "No run yet");
    }

    private void updateSpeedLabel(double v) {
        speedLabel.setText(v >= 10 ? String.format(Locale.ROOT, "%.0f/s", v) : String.format(Locale.ROOT, "%.1f/s", v));
    }

    private static double toSlider(double speed) {
        double min = Math.log(AppConfig.SPEED_MIN);
        double max = Math.log(AppConfig.SPEED_MAX);
        return (Math.log(speed) - min) / (max - min);
    }

    private static double fromSlider(double t) {
        double min = Math.log(AppConfig.SPEED_MIN);
        double max = Math.log(AppConfig.SPEED_MAX);
        double raw = Math.exp(min + t * (max - min));
        return raw < 10 ? Math.round(raw * 2) / 2.0 : Math.round(raw);
    }

    private static Button iconButton(IconType type, String tooltip) {
        Button b = new Button();
        b.setGraphic(Icons.create(type, 16));
        b.getStyleClass().addAll("ghost", "icon-button");
        b.setTooltip(new Tooltip(tooltip));
        b.setAccessibleText(tooltip);
        return b;
    }

    private static Region separator() {
        Region r = new Region();
        r.getStyleClass().add("separator-v");
        return r;
    }
}
