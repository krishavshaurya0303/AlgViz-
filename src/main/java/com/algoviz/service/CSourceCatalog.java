package com.algoviz.service;

import com.algoviz.model.CodeListing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * C listings bundled under {@code /com/algoviz/code/c/}. A trailing {@code //@2,3} tag on a
 * line names the pseudocode lines it carries out; the tag is stripped before display.
 * Keys are {@code sorting/<id>}, {@code graph/<id>}, {@code tree/<OPERATION>} and {@code list/<OPERATION>}.
 */
public final class CSourceCatalog {

    private static final Pattern TAG = Pattern.compile("\\s*//@([0-9,]+)\\s*$");
    private static final Map<String, Optional<CodeListing>> CACHE = new ConcurrentHashMap<>();

    private CSourceCatalog() {
    }

    public static Optional<CodeListing> find(String key) {
        return CACHE.computeIfAbsent(key, CSourceCatalog::load);
    }

    private static Optional<CodeListing> load(String key) {
        InputStream in = CSourceCatalog.class.getResourceAsStream("/com/algoviz/code/c/" + key + ".c");
        if (in == null) {
            return Optional.empty();
        }
        List<String> lines = new ArrayList<>();
        List<Set<Integer>> marks = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                Set<Integer> mark = new HashSet<>();
                Matcher m = TAG.matcher(line);
                if (m.find()) {
                    for (String n : m.group(1).split(",")) {
                        mark.add(Integer.parseInt(n));
                    }
                    line = line.substring(0, m.start());
                }
                lines.add(line.stripTrailing());
                marks.add(mark);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        while (!lines.isEmpty() && lines.get(lines.size() - 1).isBlank()) {
            lines.remove(lines.size() - 1);
            marks.remove(marks.size() - 1);
        }
        return Optional.of(new CodeListing(lines, marks));
    }
}
