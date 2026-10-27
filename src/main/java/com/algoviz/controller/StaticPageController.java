package com.algoviz.controller;

import com.algoviz.model.StepStatus;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;

/** Base for screens without playback (Dashboard, Library, Settings). */
public abstract class StaticPageController implements ModuleController {

    protected final AppContext context;
    protected final ReadOnlyStringWrapper status = new ReadOnlyStringWrapper("");
    private final ReadOnlyObjectWrapper<StepStatus> tone = new ReadOnlyObjectWrapper<>(StepStatus.INFO);

    protected StaticPageController(AppContext context) {
        this.context = context;
    }

    @Override
    public ReadOnlyStringProperty statusText() {
        return status.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyObjectProperty<StepStatus> statusTone() {
        return tone.getReadOnlyProperty();
    }

    protected void setTone(StepStatus value) {
        tone.set(value);
    }
}
