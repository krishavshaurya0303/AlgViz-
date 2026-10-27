package com.algoviz.controller;

import com.algoviz.config.AppConfig;
import com.algoviz.model.ModuleId;
import com.algoviz.model.StepStatus;
import com.algoviz.model.UserSettings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

import java.util.Arrays;

/** Settings screen. Every change is validated by {@link UserSettings} and saved immediately. */
public final class SettingsController extends StaticPageController {

    @FXML private CheckBox animateBox;
    @FXML private Spinner<Integer> sizeSpinner;
    @FXML private Label sizeDesc;
    @FXML private ComboBox<ModuleId> startBox;
    @FXML private CheckBox distancesBox;
    @FXML private Label activityDesc;
    @FXML private Button clearActivityButton;
    @FXML private Label fileLabel;

    private boolean loading;

    public SettingsController(AppContext context) {
        super(context);
        status.set("Saved automatically");
    }

    @FXML
    private void initialize() {
        animateBox.selectedProperty().addListener((o, a, v) -> {
            if (!loading) {
                context.settings().setAnimateTransitions(v);
                save();
            }
        });
        sizeDesc.setText("Number of bars generated when the Sorting Studio first opens ("
                + AppConfig.SORT_MIN_SIZE + "–" + AppConfig.SORT_MAX_SIZE + ").");
        sizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                AppConfig.SORT_MIN_SIZE, AppConfig.SORT_MAX_SIZE, AppConfig.SORT_DEFAULT_SIZE));
        // Typing an invalid number reverts to the last valid value instead of throwing.
        sizeSpinner.getValueFactory().setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer v) {
                return v == null ? "" : v.toString();
            }

            @Override
            public Integer fromString(String s) {
                try {
                    int v = Integer.parseInt(s.trim());
                    return Math.max(AppConfig.SORT_MIN_SIZE, Math.min(AppConfig.SORT_MAX_SIZE, v));
                } catch (NumberFormatException e) {
                    return sizeSpinner.getValue();
                }
            }
        });
        sizeSpinner.focusedProperty().addListener((o, a, focused) -> {
            if (!focused) {
                sizeSpinner.increment(0); // commit typed text
            }
        });
        sizeSpinner.valueProperty().addListener((o, a, v) -> {
            if (!loading && v != null) {
                context.settings().setDefaultArraySize(v);
                save();
            }
        });
        startBox.getItems().setAll(Arrays.stream(ModuleId.values()).filter(m -> m != ModuleId.SETTINGS).toList());
        startBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(ModuleId m) {
                return m == null ? "" : m.title();
            }

            @Override
            public ModuleId fromString(String s) {
                return null;
            }
        });
        startBox.setOnAction(e -> {
            if (!loading && startBox.getValue() != null) {
                context.settings().setStartModule(startBox.getValue());
                save();
            }
        });
        distancesBox.selectedProperty().addListener((o, a, v) -> {
            if (!loading) {
                context.settings().setShowGridDistances(v);
                save();
            }
        });
        fileLabel.setText(context.settingsService().file().toString());
    }

    @Override
    public void onShow(NavigationRequest request) {
        loading = true;
        UserSettings s = context.settings();
        animateBox.setSelected(s.animateTransitions());
        sizeSpinner.getValueFactory().setValue(s.defaultArraySize());
        startBox.setValue(s.startModule());
        distancesBox.setSelected(s.showGridDistances());
        updateActivity();
        loading = false;
    }

    private void updateActivity() {
        UserSettings s = context.settings();
        boolean has = s.lastModule() != null;
        activityDesc.setText(has ? "Remembered: " + s.lastModule().title()
                + ". Used only for the dashboard's Continue learning card." : "Nothing is stored.");
        clearActivityButton.setDisable(!has);
    }

    private void save() {
        context.saveSettings();
    }

    @FXML
    private void clearActivity() {
        context.settings().clearActivity();
        save();
        updateActivity();
        context.notify("Recent activity cleared.", StepStatus.SUCCESS);
    }

    @FXML
    private void resetSettings() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Defaults and the start module will be reset.",
                ButtonType.CANCEL, ButtonType.OK);
        confirm.setTitle("Reset settings");
        confirm.setHeaderText("Reset all settings to their defaults?");
        confirm.getDialogPane().getStylesheets().addAll(animateBox.getScene().getStylesheets());
        confirm.initOwner(animateBox.getScene().getWindow());
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            context.settings().resetPreferences();
            save();
            onShow(NavigationRequest.NONE);
            context.notify("Settings restored to defaults.", StepStatus.SUCCESS);
        }
    }

    @Override
    public String helpText() {
        return "Changes are saved immediately to a small file in your home folder. If the file is missing or "
                + "damaged, AlgoViz starts with the defaults.";
    }
}
