package com.algoviz.ui;

import com.algoviz.AlgoVizApplication;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test of the real JavaFX application: launches the window, visits
 * every module and drives playback through the UI controls. Needs a display
 * (run with xvfb-run on headless machines); skipped otherwise.
 */
@EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".+")
class UiSmokeTest {

    @TempDir
    static Path home;
    private static String oldHome;
    private static Scene scene;
    private static final AtomicReference<Throwable> fxError = new AtomicReference<>();

    @BeforeAll
    static void launch() throws Exception {
        oldHome = System.getProperty("user.home");
        System.setProperty("user.home", home.toString()); // keep the real settings file untouched
        CompletableFuture<Void> started = new CompletableFuture<>();
        Platform.startup(() -> {
            Thread.currentThread().setUncaughtExceptionHandler((t, e) -> fxError.set(e));
            try {
                Stage stage = new Stage();
                new AlgoVizApplication().start(stage);
                Thread.currentThread().setUncaughtExceptionHandler((t, e) -> fxError.set(e));
                scene = stage.getScene();
                started.complete(null);
            } catch (Exception e) {
                started.completeExceptionally(e);
            }
        });
        started.get(30, TimeUnit.SECONDS);
    }

    @AfterAll
    static void shutdown() {
        Platform.runLater(Platform::exit);
        System.setProperty("user.home", oldHome);
    }

    private static <T> T fx(java.util.concurrent.Callable<T> c) throws Exception {
        CompletableFuture<T> f = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                f.complete(c.call());
            } catch (Throwable t) {
                f.completeExceptionally(t);
            }
        });
        return f.get(20, TimeUnit.SECONDS);
    }

    private static boolean showing(Node n) {
        for (Node p = n; p != null; p = p.getParent()) {
            if (!p.isVisible()) {
                return false;
            }
        }
        return n.getScene() != null;
    }

    private static void click(String textOrTooltip) throws Exception {
        boolean found = fx(() -> {
            for (Node n : scene.getRoot().lookupAll("*")) {
                if (n instanceof ButtonBase b && showing(b) && !b.isDisabled()) {
                    Tooltip t = b.getTooltip();
                    if (textOrTooltip.equals(((Labeled) b).getText())
                            || t != null && t.getText().startsWith(textOrTooltip)) {
                        b.fire();
                        return true;
                    }
                }
            }
            return false;
        });
        assertTrue(found, "control not found or disabled: " + textOrTooltip);
    }

    /** Fires the visible play/pause button. */
    private static void togglePlay() throws Exception {
        fx(() -> {
            for (Node n : scene.getRoot().lookupAll(".play-button")) {
                if (showing(n)) {
                    ((ButtonBase) n).fire();
                    return null;
                }
            }
            throw new AssertionError("play button not found");
        });
    }

    private static String stepLabel() throws Exception {
        return fx(() -> {
            for (Node n : scene.getRoot().lookupAll(".step-label")) {
                if (showing(n)) {
                    return ((Label) n).getText();
                }
            }
            return null;
        });
    }

    private static String headerTitle() throws Exception {
        return fx(() -> ((Label) scene.getRoot().lookup(".header-title")).getText());
    }

    private static void type(String prompt, String value) throws Exception {
        fx(() -> {
            for (Node n : scene.getRoot().lookupAll(".text-field")) {
                if (n instanceof TextField tf && showing(tf) && prompt.equals(tf.getPromptText())) {
                    tf.setText(value);
                    return null;
                }
            }
            throw new AssertionError("field not found " + prompt);
        });
    }

    private static void waitMs(long ms) throws InterruptedException {
        Thread.sleep(ms);
    }

    @Test
    void everyModuleOpensAndPlaybackWorks() throws Exception {
        assertEquals("Dashboard", headerTitle());
        for (String m : new String[]{"Graph Explorer", "Tree Visualizer", "Linked List Lab", "Algorithm Library",
                "Settings", "Sorting Studio"}) {
            click(m);
            assertEquals(m, headerTitle());
        }

        // Sorting: trace is built in the background, then play / pause / step / reset through the bar.
        waitMs(800);
        assertTrue(stepLabel().startsWith("Step 0 / "), stepLabel());
        click("Step forward");
        click("Step forward");
        assertTrue(stepLabel().startsWith("Step 2 / "));
        click("Step backward");
        assertTrue(stepLabel().startsWith("Step 1 / "));
        togglePlay();
        waitMs(900);
        togglePlay();
        String paused = stepLabel();
        waitMs(700);
        assertEquals(paused, stepLabel(), "pause must stop playback");
        click("Reset");
        assertTrue(stepLabel().startsWith("Step 0 / "));
        click("Jump to the final step");
        String s = stepLabel();
        String[] parts = s.replace("Step ", "").split(" / ");
        assertEquals(parts[1], parts[0]);

        // Tree: sample + delete, then step backward through the operation.
        click("Tree Visualizer");
        click("Sample tree");
        type("e.g. 42", "abc");
        click("Insert");
        String msg = fx(() -> {
            for (Node n : scene.getRoot().lookupAll(".inline-message")) {
                if (showing(n) && !((Label) n).getText().isEmpty()) {
                    return ((Label) n).getText();
                }
            }
            return "";
        });
        assertTrue(msg.contains("not a whole number"), msg);
        type("e.g. 42", "1000");
        click("Insert");
        type("e.g. 42", "3");
        click("Insert");
        click("Jump to the final step");
        click("Step backward");
        assertNotNull(stepLabel());

        // Linked list: sample, reverse, finish.
        click("Linked List Lab");
        click("Sample");
        click("Reverse");
        click("Jump to the final step");
        assertTrue(fx(() -> ((Label) scene.getRoot().lookup(".explanation-text")).getText()).startsWith("The list is reversed"));

        // Graph: clear board and run to the end.
        click("Graph Explorer");
        click("Clear board");
        waitMs(500);
        click("Jump to the final step");
        assertTrue(fx(() -> ((Label) scene.getRoot().lookup(".explanation-text")).getText()).startsWith("Path found"));

        click("Dashboard");
        assertEquals("Dashboard", headerTitle());
        waitMs(200);
        if (fxError.get() != null) {
            throw new AssertionError("Exception on FX thread", fxError.get());
        }
    }
}
