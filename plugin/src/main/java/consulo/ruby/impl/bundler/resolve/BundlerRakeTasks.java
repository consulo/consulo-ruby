package consulo.ruby.impl.bundler.resolve;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses {@code rake -AT}: {@code rake name[args]   # description}.
 *
 * @author VISTALL
 * @since 2026-11-01
 */
public final class BundlerRakeTasks {
    private static final Pattern TASK = Pattern.compile("^rake (\\S+?)(\\[[^\\]]*\\])?(?:\\s+#\\s*(.*))?\\s*$");

    private BundlerRakeTasks() {
    }

    public static Map<String, String> parse(String output) {
        Map<String, String> tasks = new LinkedHashMap<>();
        for (String line : output.split("\\R")) {
            Matcher matcher = TASK.matcher(line);
            if (matcher.matches()) {
                String description = matcher.group(3);
                tasks.putIfAbsent(matcher.group(1), description == null ? "" : description.trim());
            }
        }
        return tasks;
    }
}
