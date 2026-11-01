package consulo.ruby.impl.bundler.resolve;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads the locked gems of a {@code Gemfile.lock} without Ruby, for when the gems are not installed.
 *
 * @author VISTALL
 * @since 2026-11-01
 */
public final class BundlerGemfileLock {
    // "    name (1.2.3)" or "    name (1.2.3-x86_64-linux)" - four spaces are a spec, six are its dependencies
    private static final Pattern SPEC = Pattern.compile("^ {4}([^\\s(]+) \\(([^)]+)\\)\\s*$");
    private static final Pattern PLATFORM_SUFFIX = Pattern.compile("-(x86|x64|arm|aarch64|universal|java|mswin|mingw|darwin|linux).*$");

    private BundlerGemfileLock() {
    }

    public static List<BundlerGem> read(Path lockFile) {
        List<String> lines;
        try {
            lines = Files.readAllLines(lockFile);
        }
        catch (IOException e) {
            return List.of();
        }

        Map<String, BundlerGem> gems = new LinkedHashMap<>();
        boolean inSpecs = false;
        for (String line : lines) {
            if (!line.startsWith(" ")) {
                inSpecs = false;
                continue;
            }
            if (line.trim().equals("specs:")) {
                inSpecs = true;
                continue;
            }
            if (!inSpecs) {
                continue;
            }
            Matcher matcher = SPEC.matcher(line);
            if (matcher.matches()) {
                String name = matcher.group(1);
                String version = PLATFORM_SUFFIX.matcher(matcher.group(2)).replaceFirst("");
                gems.putIfAbsent(name, new BundlerGem(name, version, "", List.of()));
            }
        }
        return new ArrayList<>(gems.values());
    }
}
