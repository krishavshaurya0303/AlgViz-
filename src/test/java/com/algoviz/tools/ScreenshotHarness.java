package com.algoviz.tools;

import com.algoviz.AlgoVizApplication;
import com.algoviz.ui.render.ResizableCanvas;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * Developer tool (not shipped): launches the real application and executes a
 * small script of UI actions, saving screenshots. Used to verify the UI under Xvfb.
 * Commands: nav:TEXT | click:TEXT | type:PROMPT_SUBSTRING=VALUE | combo:INDEX=ITEM | key:CODE
 *           | wait:MS | shot:FILE | size:WxH | press:X,Y | move:X,Y | release | hoverat:X,Y  (mouse, scene coordinates)
 *           | enter:TEXT  (pointer enters a button)
 */
public final class ScreenshotHarness extends Application {

    private static String[] script;
    private Scene scene;
    private Stage stage;

    public static void main(String[] args) {
        script = args;
        launch();
    }

    @Override
    public void start(Stage s) throws Exception {
        this.stage = s;
        new AlgoVizApplication().start(s);
        scene = s.getScene();
        Deque<String> q = new ArrayDeque<>(Arrays.asList(script));
        next(q, 800);
    }

    private void next(Deque<String> q, double delay) {
        Timeline t = new Timeline(new KeyFrame(Duration.millis(delay), e -> run(q)));
        t.play();
    }

    private void run(Deque<String> q) {
        if (q.isEmpty()) {
            Platform.exit();
            return;
        }
        String cmd = q.poll();
        int c = cmd.indexOf(':');
        String op = c < 0 ? cmd : cmd.substring(0, c);
        String arg = c < 0 ? "" : cmd.substring(c + 1);
        double delay = 150;
        try {
            switch (op) {
                case "nav", "click" -> {
                    ButtonBase b = find(arg);
                    if (b == null) {
                        System.err.println("NOT FOUND: " + arg);
                    } else {
                        b.fire();
                    }
                }
                case "type" -> {
                    String[] kv = arg.split("=", 2);
                    for (Node n : scene.getRoot().lookupAll(".text-field")) {
                        if (n instanceof TextField tf && tf.isVisible() && tf.getPromptText() != null
                                && tf.getPromptText().contains(kv[0])) {
                            tf.setText(kv[1]);
                            tf.fireEvent(new ActionEvent());
                            break;
                        }
                    }
                }
                case "combo" -> {
                    String[] kv = arg.split("=", 2);
                    int idx = Integer.parseInt(kv[0]);
                    int i = 0;
                    for (Node n : scene.getRoot().lookupAll(".combo-box")) {
                        if (n instanceof ComboBox<?> cb && cb.isVisible() && cb.getScene() != null && n.isVisible()
                                && isShowing(n)) {
                            if (i++ == idx) {
                                for (Object item : cb.getItems()) {
                                    String label = cb.getConverter() != null ? cb.getConverter().toString(cast(item)) : String.valueOf(item);
                                    if (label != null && label.contains(kv[1]) || String.valueOf(item).contains(kv[1])) {
                                        select(cb, item);
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                case "key" -> {
                    KeyCode code = KeyCode.valueOf(arg);
                    scene.getRoot().requestFocus();
                    KeyEvent.fireEvent(scene.getRoot(), new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code,
                            false, false, false, false));
                }
                case "press", "move" -> {
                    String[] xy = arg.split(",");
                    pointer(op.equals("press") ? MouseEvent.MOUSE_PRESSED : MouseEvent.MOUSE_DRAGGED,
                            Double.parseDouble(xy[0]), Double.parseDouble(xy[1]));
                }
                case "release" -> pointer(MouseEvent.MOUSE_RELEASED, lastX, lastY);
                case "hoverat" -> {
                    String[] xy = arg.split(",");
                    pointer(MouseEvent.MOUSE_MOVED, Double.parseDouble(xy[0]), Double.parseDouble(xy[1]));
                }
                case "enter" -> {
                    ButtonBase b = find(arg);
                    if (b == null) {
                        System.err.println("NOT FOUND: " + arg);
                    } else {
                        Point2D mid = b.localToScene(b.getWidth() / 2, b.getHeight() / 2);
                        b.fireEvent(new MouseEvent(MouseEvent.MOUSE_ENTERED, mid.getX(), mid.getY(), mid.getX(), mid.getY(),
                                MouseButton.NONE, 0, false, false, false, false, false, false, false, false, false,
                                false, null));
                    }
                }
                case "wait" -> delay = Double.parseDouble(arg);
                case "size" -> {
                    String[] wh = arg.split("x");
                    stage.setWidth(Double.parseDouble(wh[0]));
                    stage.setHeight(Double.parseDouble(wh[1]));
                    delay = 400;
                }
                case "shot" -> {
                    WritableImage img = scene.snapshot(null);
                    ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", new File(arg));
                    System.out.println("saved " + arg);
                }
                default -> System.err.println("unknown command " + cmd);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        next(q, delay);
    }

    private double lastX;
    private double lastY;

    /** Sends a synthetic primary-button mouse event at scene point (x, y) to the visible canvas under it. */
    private void pointer(EventType<MouseEvent> type, double x, double y) {
        lastX = x;
        lastY = y;
        for (Node n : scene.getRoot().lookupAll("*")) {
            if (n instanceof ResizableCanvas c && isShowing(c)) {
                Point2D local = c.sceneToLocal(x, y);
                if (c.contains(local)) {
                    // x/y are scene coordinates here; dispatch converts them to the canvas's local ones.
                    c.fireEvent(new MouseEvent(type, x, y, x, y, MouseButton.PRIMARY, 1,
                            false, false, false, false, true, false, false, false, false, false, null));
                    return;
                }
            }
        }
        System.err.println("NO CANVAS AT " + x + "," + y);
    }

    private static boolean isShowing(Node n) {
        for (Node p = n; p != null; p = p.getParent()) {
            if (!p.isVisible()) {
                return false;
            }
        }
        return n.getScene() != null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object o) {
        return (T) o;
    }

    @SuppressWarnings("unchecked")
    private static <T> void select(ComboBox<T> cb, Object item) {
        cb.getSelectionModel().select((T) item);
        cb.fireEvent(new ActionEvent());
    }

    private ButtonBase find(String text) {
        for (Node n : scene.getRoot().lookupAll("*")) {
            if (n instanceof ButtonBase b && isShowing(b) && !b.isDisabled()) {
                Tooltip tip = b.getTooltip();
                if (text.equals(((Labeled) b).getText()) || tip != null && (tip.getText().equals(text) || tip.getText().startsWith(text + " "))) {
                    return b;
                }
            }
        }
        return null;
    }
}
