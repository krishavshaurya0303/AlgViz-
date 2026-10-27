package com.algoviz.ui.component;

import com.algoviz.model.StepStatus;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/** "What's happening" card: the current step's explanation, tinted by its status. */
public final class ExplanationCard extends HBox {

    private final Label title = new Label("WHAT'S HAPPENING");
    private final Label text = new Label();

    public ExplanationCard() {
        getStyleClass().add("explanation-card");
        setSpacing(12);
        Region accent = new Region();
        accent.getStyleClass().add("explanation-accent");
        title.getStyleClass().add("card-title");
        text.getStyleClass().add("explanation-text");
        text.setMinHeight(Region.USE_PREF_SIZE);
        VBox body = new VBox(6, title, text);
        HBox.setHgrow(body, Priority.ALWAYS);
        getChildren().addAll(accent, body);
        show("Choose an algorithm and press Next (→).", StepStatus.INFO);
    }

    public void setTitle(String value) {
        title.setText(value.toUpperCase());
    }

    public void show(String explanation, StepStatus status) {
        text.setText(explanation);
        getStyleClass().removeIf(c -> c.startsWith("tone-"));
        getStyleClass().add("tone-" + status.name().toLowerCase());
    }
}
