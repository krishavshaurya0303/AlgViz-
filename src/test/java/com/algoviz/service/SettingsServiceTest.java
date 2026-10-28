package com.algoviz.service;

import com.algoviz.config.AppConfig;
import com.algoviz.model.ModuleId;
import com.algoviz.model.UserSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsServiceTest {

    @TempDir
    Path dir;

    @Test
    void missingFileGivesDefaults() {
        UserSettings s = new SettingsService(dir.resolve("nope/settings.properties")).load();
        assertEquals(AppConfig.SPEED_DEFAULT, s.playbackSpeed());
        assertEquals(ModuleId.DASHBOARD, s.startModule());
        assertNull(s.lastModule());
    }

    @Test
    void roundTrip() {
        SettingsService svc = new SettingsService(dir.resolve("a/settings.properties"));
        UserSettings s = new UserSettings();
        s.setPlaybackSpeed(12);
        s.setDefaultArraySize(15);
        s.setStartModule(ModuleId.GRAPH);
        s.setAnimateTransitions(false);
        s.recordActivity(ModuleId.SORTING, "quick-sort", 1234L);
        assertTrue(svc.save(s));
        UserSettings back = svc.load();
        assertEquals(12, back.playbackSpeed());
        assertEquals(15, back.defaultArraySize());
        assertEquals(ModuleId.GRAPH, back.startModule());
        assertFalse(back.animateTransitions());
        assertEquals(ModuleId.SORTING, back.lastModule());
        assertEquals("quick-sort", back.lastAlgorithmId());
        assertEquals(1234L, back.lastOpenedEpochMillis());
    }

    @Test
    void malformedValuesFallBackToDefaults() throws Exception {
        Path f = dir.resolve("settings.properties");
        Files.writeString(f, "playback.speed=fast\nsorting.defaultSize=100000\nstartModule=NOPE\n"
                + "animateTransitions=maybe\nrecent.module=TREE\nrecent.time=x\n");
        UserSettings s = new SettingsService(f).load();
        assertEquals(AppConfig.SPEED_DEFAULT, s.playbackSpeed());
        assertEquals(AppConfig.SORT_MAX_SIZE, s.defaultArraySize()); // clamped
        assertEquals(ModuleId.DASHBOARD, s.startModule());
        assertTrue(s.animateTransitions());
        assertEquals(ModuleId.TREE, s.lastModule());
        assertEquals(0L, s.lastOpenedEpochMillis());
    }

    @Test
    void unreadableFileDoesNotThrow() throws Exception {
        Path f = dir.resolve("settings.properties");
        Files.createDirectories(f); // a non-empty directory where a file is expected
        Files.writeString(f.resolve("blocker"), "x");
        SettingsService svc = new SettingsService(f);
        UserSettings s = svc.load();
        assertEquals(AppConfig.SPEED_DEFAULT, s.playbackSpeed());
        assertTrue(svc.lastError() != null);
        assertFalse(svc.save(s));
    }
}
