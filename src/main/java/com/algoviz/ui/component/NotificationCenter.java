package com.algoviz.ui.component;

import com.algoviz.model.StepStatus;
import com.algoviz.ui.animation.Easing;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Non-blocking toast notifications stacked in the bottom-right corner. */
public final class NotificationCenter extends VBox {

    private static final int MAX_VISIBLE = 3;

    public NotificationCenter() {
        getStyleClass().add("toast-layer");
        setAlignment(Pos.BOTTOM_RIGHT);
        setPickOnBounds(false);
        setMouseTransparent(false);
    }

    public void show(String message, StepStatus tone) {
        Region bar = new Region();
        bar.getStyleClass().add("toast-bar");
        bar.setMinHeight(18);
        Label text = new Label(message);
        HBox toast = new HBox(bar, text);
        toast.getStyleClass().addAll("toast", "tone-" + tone.name().toLowerCase());
        toast.setMaxWidth(380);
        toast.setOnMouseClicked(e -> getChildren().remove(toast));
        if (getChildren().size() >= MAX_VISIBLE) {
            getChildren().remove(0);
        }
        getChildren().add(toast);

        // Enter: rise 8px while fading in (ease-out). Exit: a quicker fade, so it never lingers.
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setInterpolator(Easing.OUT);
        TranslateTransition rise = new TranslateTransition(Duration.millis(200), toast);
        rise.setFromY(8);
        rise.setToY(0);
        rise.setInterpolator(Easing.OUT);
        ParallelTransition in = new ParallelTransition(fadeIn, rise);
        PauseTransition hold = new PauseTransition(Duration.seconds(tone == StepStatus.FAILURE ? 5 : 3.2));
        FadeTransition out = new FadeTransition(Duration.millis(150), toast);
        out.setToValue(0);
        SequentialTransition seq = new SequentialTransition(in, hold, out);
        seq.setOnFinished(e -> getChildren().remove(toast));
        seq.play();
    }
}
