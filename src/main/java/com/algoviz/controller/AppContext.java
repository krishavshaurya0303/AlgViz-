package com.algoviz.controller;

import com.algoviz.model.ModuleId;
import com.algoviz.model.StepStatus;
import com.algoviz.model.UserSettings;
import com.algoviz.service.AlgorithmCatalog;
import com.algoviz.service.SettingsService;

import java.util.Objects;

/**
 * Shared services handed to every controller through the FXML controller
 * factory (constructor injection). This replaces global singletons: each
 * dependency has exactly one owner — the application — and is passed explicitly.
 */
public final class AppContext {

    /** Navigation callback implemented by the main shell. */
    public interface Navigator {
        void open(ModuleId module, NavigationRequest request);
    }

    /** Toast notifications. */
    public interface Notifier {
        void notify(String message, StepStatus tone);
    }

    private final AlgorithmCatalog catalog;
    private final SettingsService settingsService;
    private final UserSettings settings;
    private Navigator navigator = (m, r) -> { };
    private Notifier notifier = (m, t) -> { };

    public AppContext(AlgorithmCatalog catalog, SettingsService settingsService, UserSettings settings) {
        this.catalog = Objects.requireNonNull(catalog);
        this.settingsService = Objects.requireNonNull(settingsService);
        this.settings = Objects.requireNonNull(settings);
    }

    public AlgorithmCatalog catalog() {
        return catalog;
    }

    public UserSettings settings() {
        return settings;
    }

    public SettingsService settingsService() {
        return settingsService;
    }

    /** Persists the settings; reports a failure to the user once. */
    public void saveSettings() {
        if (!settingsService.save(settings)) {
            notifier.notify(settingsService.lastError(), StepStatus.WARNING);
        }
    }

    public void navigate(ModuleId module) {
        navigator.open(module, NavigationRequest.NONE);
    }

    public void navigate(ModuleId module, NavigationRequest request) {
        navigator.open(module, request);
    }

    public void notify(String message, StepStatus tone) {
        notifier.notify(message, tone);
    }

    void setNavigator(Navigator navigator) {
        this.navigator = navigator;
    }

    void setNotifier(Notifier notifier) {
        this.notifier = notifier;
    }

    /** Records that the user really opened a module / algorithm (for "Continue learning"). */
    public void recordActivity(ModuleId module, String algorithmId) {
        settings.recordActivity(module, algorithmId, System.currentTimeMillis());
        saveSettings();
    }
}
