package com.algoviz.model;

import com.algoviz.config.AppConfig;

/**
 * User preferences. Plain mutable bean with validation in the setters;
 * persisted by {@link com.algoviz.service.SettingsService}.
 */
public final class UserSettings {

    private double playbackSpeed = AppConfig.SPEED_DEFAULT;
    private int defaultArraySize = AppConfig.SORT_DEFAULT_SIZE;
    private ModuleId startModule = ModuleId.DASHBOARD;
    private boolean animateTransitions = true;
    private boolean showGridDistances = true;

    // Recent activity — recorded only when the user actually opens a module.
    private ModuleId lastModule;
    private String lastAlgorithmId;
    private long lastOpenedEpochMillis;

    public double playbackSpeed() {
        return playbackSpeed;
    }

    public void setPlaybackSpeed(double speed) {
        this.playbackSpeed = Math.max(AppConfig.SPEED_MIN, Math.min(AppConfig.SPEED_MAX, speed));
    }

    public int defaultArraySize() {
        return defaultArraySize;
    }

    public void setDefaultArraySize(int size) {
        this.defaultArraySize = Math.max(AppConfig.SORT_MIN_SIZE, Math.min(AppConfig.SORT_MAX_SIZE, size));
    }

    public ModuleId startModule() {
        return startModule;
    }

    public void setStartModule(ModuleId module) {
        this.startModule = module == null || module == ModuleId.SETTINGS ? ModuleId.DASHBOARD : module;
    }

    public boolean animateTransitions() {
        return animateTransitions;
    }

    public void setAnimateTransitions(boolean value) {
        this.animateTransitions = value;
    }

    public boolean showGridDistances() {
        return showGridDistances;
    }

    public void setShowGridDistances(boolean value) {
        this.showGridDistances = value;
    }

    public ModuleId lastModule() {
        return lastModule;
    }

    public String lastAlgorithmId() {
        return lastAlgorithmId;
    }

    public long lastOpenedEpochMillis() {
        return lastOpenedEpochMillis;
    }

    public void recordActivity(ModuleId module, String algorithmId, long epochMillis) {
        this.lastModule = module;
        this.lastAlgorithmId = algorithmId;
        this.lastOpenedEpochMillis = epochMillis;
    }

    public void clearActivity() {
        lastModule = null;
        lastAlgorithmId = null;
        lastOpenedEpochMillis = 0;
    }

    /** Restores every preference to its default (recent activity is kept). */
    public void resetPreferences() {
        playbackSpeed = AppConfig.SPEED_DEFAULT;
        defaultArraySize = AppConfig.SORT_DEFAULT_SIZE;
        startModule = ModuleId.DASHBOARD;
        animateTransitions = true;
        showGridDistances = true;
    }
}
