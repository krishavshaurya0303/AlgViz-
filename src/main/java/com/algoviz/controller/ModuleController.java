package com.algoviz.controller;

import com.algoviz.model.StepStatus;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;

import java.util.List;

/** Contract between the main shell and each screen. */
public interface ModuleController {

    /** Called each time the module becomes visible. */
    default void onShow(NavigationRequest request) {
    }

    /** Called before {@link #onShow} so the module can pick up changed settings. */
    default void applySettings() {
    }

    /** Called when the user navigates away; visualizers pause playback here. */
    default void onHide() {
    }

    /** Contextual buttons shown in the header while this module is visible. */
    default List<Node> headerActions() {
        return List.of();
    }

    /** Short status text for the header chip. */
    ReadOnlyStringProperty statusText();

    ReadOnlyObjectProperty<StepStatus> statusTone();

    /** Keyboard shortcuts; return true if the event was handled. */
    default boolean handleKey(KeyEvent event) {
        return false;
    }

    /** Help text shown by the header's Help button. */
    default String helpText() {
        return "";
    }
}
