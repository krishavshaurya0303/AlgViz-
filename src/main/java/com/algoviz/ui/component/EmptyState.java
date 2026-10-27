package com.algoviz.ui.component;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/** Centered icon + title + text + optional action, shown when there is nothing to visualize. */
public final class EmptyState extends VBox {

    private final Label title = new Label();
    private final Label text = new Label();
    private final Button action = new Button();

    public EmptyState(IconType icon, String titleText, String body) {
        getStyleClass().add("empty-state");
        title.getStyleClass().add("empty-title");
        text.getStyleClass().add("empty-text");
        title.setText(titleText);
        text.setText(body);
        action.getStyleClass().add("primary");
        action.setVisible(false);
        action.setManaged(false);
        getChildren().addAll(Icons.create(icon, 34), title, text, action);
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setPickOnBounds(false);
    }

    public void setContent(String titleText, String body) {
        title.setText(titleText);
        text.setText(body);
    }

    public void setAction(String label, Runnable run) {
        action.setText(label);
        action.setOnAction(e -> run.run());
        action.setVisible(true);
        action.setManaged(true);
    }
}
