package com.algoviz.ui.theme;

import javafx.scene.text.Font;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads the bundled Inter (UI) and JetBrains Mono (code) fonts, both licensed
 * under the SIL Open Font License. If loading fails, CSS falls back to system fonts.
 */
public final class FontLoader {

    private static final Logger LOG = Logger.getLogger(FontLoader.class.getName());
    private static final String[] FONTS = {
            "Inter-Regular.otf", "Inter-Medium.otf", "Inter-SemiBold.otf", "Inter-Bold.otf",
            "JetBrainsMono-Regular.ttf", "JetBrainsMono-Medium.ttf"
    };

    private FontLoader() {
    }

    public static void loadBundledFonts() {
        for (String name : FONTS) {
            try (InputStream in = FontLoader.class.getResourceAsStream("/com/algoviz/fonts/" + name)) {
                if (in == null) {
                    LOG.warning("Bundled font missing: " + name);
                    continue;
                }
                Font f = Font.loadFont(in, 13);
                if (f == null) {
                    LOG.warning("Could not load font " + name);
                }
            } catch (IOException e) {
                LOG.log(Level.WARNING, "Could not read font " + name, e);
            }
        }
    }

    /** Monospace font for canvas labels. */
    public static Font mono(double size) {
        return Font.font("JetBrains Mono", size);
    }

    public static Font ui(double size) {
        return Font.font("Inter", size);
    }

    public static Font uiBold(double size) {
        return Font.font("Inter", javafx.scene.text.FontWeight.BOLD, size);
    }
}
