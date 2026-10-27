package com.algoviz.ui.component;

import com.algoviz.model.StepStatus;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
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

        FadeTransition in = new FadeTransition(Duration.millis(160), toast);
        in.setFromValue(0);
        in.setToValue(1);
        PauseTransition hold = new PauseTransition(Duration.seconds(tone == StepStatus.FAILURE ? 5 : 3.2));
        FadeTransition out = new FadeTransition(Duration.millis(260), toast);
        out.setToValue(0);
        SequentialTransition seq = new SequentialTransition(in, hold, out);
        seq.setOnFinished(e -> getChildren().remove(toast));
        seq.play();
    }
}
