package com.algoviz.service;

import com.algoviz.model.ModuleId;
import com.algoviz.model.UserSettings;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads and saves {@link UserSettings} as a small properties file in a
 * per-user directory (~/.algoviz/settings.properties). Missing, unreadable or
 * malformed files never crash the application: invalid entries fall back to
 * defaults and the problem is logged.
 */
public final class SettingsService {

    private static final Logger LOG = Logger.getLogger(SettingsService.class.getName());

    private final Path file;
    private String lastError;

    public SettingsService(Path file) {
        this.file = file;
    }

    /** Default location: {@code <user.home>/.algoviz/settings.properties}. */
    public static SettingsService createDefault() {
        return new SettingsService(Path.of(System.getProperty("user.home"), ".algoviz", "settings.properties"));
    }

    public Path file() {
        return file;
    }

    /** Message of the last load/save problem, or null. */
    public String lastError() {
        return lastError;
    }

    public UserSettings load() {
        UserSettings s = new UserSettings();
        lastError = null;
        if (!Files.exists(file)) {
            return s;
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            p.load(in);
        } catch (IOException | IllegalArgumentException e) {
            lastError = "Could not read settings (" + e.getMessage() + "); defaults are used.";
            LOG.log(Level.WARNING, lastError, e);
            return s;
        }
        s.setPlaybackSpeed(parseDouble(p.getProperty("playback.speed"), s.playbackSpeed()));
        s.setDefaultArraySize(parseInt(p.getProperty("sorting.defaultSize"), s.defaultArraySize()));
        s.setStartModule(parseModule(p.getProperty("startModule"), s.startModule()));
        s.setAnimateTransitions(parseBool(p.getProperty("animateTransitions"), s.animateTransitions()));
        s.setShowGridDistances(parseBool(p.getProperty("graph.showDistances"), s.showGridDistances()));
        ModuleId last = parseModule(p.getProperty("recent.module"), null);
        if (last != null) {
            s.recordActivity(last, p.getProperty("recent.algorithm"),
                    parseLong(p.getProperty("recent.time"), 0L));
        }
        return s;
    }

    /** @return true on success */
    public boolean save(UserSettings s) {
        Properties p = new Properties();
        p.setProperty("playback.speed", Double.toString(s.playbackSpeed()));
        p.setProperty("sorting.defaultSize", Integer.toString(s.defaultArraySize()));
        p.setProperty("startModule", s.startModule().name());
        p.setProperty("animateTransitions", Boolean.toString(s.animateTransitions()));
        p.setProperty("graph.showDistances", Boolean.toString(s.showGridDistances()));
        if (s.lastModule() != null) {
            p.setProperty("recent.module", s.lastModule().name());
            if (s.lastAlgorithmId() != null) {
                p.setProperty("recent.algorithm", s.lastAlgorithmId());
            }
            p.setProperty("recent.time", Long.toString(s.lastOpenedEpochMillis()));
        }
        try {
            Files.createDirectories(file.getParent());
            // Write to a temporary file first so a crash never leaves a half-written settings file.
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            try (OutputStream out = Files.newOutputStream(tmp)) {
                p.store(out, "AlgoViz user settings");
            }
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            lastError = null;
            return true;
        } catch (IOException e) {
            lastError = "Could not save settings: " + e.getMessage();
            LOG.log(Level.WARNING, lastError, e);
            return false;
        }
    }

    private static double parseDouble(String v, double fallback) {
        try {
            return v == null ? fallback : Double.parseDouble(v.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static int parseInt(String v, int fallback) {
        try {
            return v == null ? fallback : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static long parseLong(String v, long fallback) {
        try {
            return v == null ? fallback : Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static boolean parseBool(String v, boolean fallback) {
        if (v == null) {
            return fallback;
        }
        String t = v.trim().toLowerCase();
        if (t.equals("true")) {
            return true;
        }
        if (t.equals("false")) {
            return false;
        }
        return fallback;
    }

    private static ModuleId parseModule(String v, ModuleId fallback) {
        if (v == null) {
            return fallback;
        }
        try {
            return ModuleId.valueOf(v.trim());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
