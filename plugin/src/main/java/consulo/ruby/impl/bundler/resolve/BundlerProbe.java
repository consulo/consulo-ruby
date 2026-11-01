package consulo.ruby.impl.bundler.resolve;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Asks Bundler itself for the resolved gems, so groups, platforms, path and git sources and the gem
 * directories of the interpreter are taken into account.
 *
 * @author VISTALL
 * @since 2026-11-01
 */
public final class BundlerProbe {
    public static final String MARKER = "#CONSULO_BUNDLER#";

    public static final String SCRIPT = String.join("\n",
        "require 'json'",
        "out = { 'ruby' => RUBY_VERSION, 'dependencies' => [], 'gems' => [] }",
        "begin",
        "  require 'bundler'",
        "  definition = Bundler.definition",
        "  out['dependencies'] = definition.dependencies.map(&:name)",
        "  begin",
        "    out['gems'] = definition.specs.map do |s|",
        "      { 'name' => s.name, 'version' => s.version.to_s, 'path' => s.full_gem_path, 'requirePaths' => s.full_require_paths }",
        "    end",
        "  rescue Bundler::GemNotFound => e",
        "    out['missing'] = e.message",
        "  end",
        "rescue StandardError, LoadError => e",
        "  out['error'] = e.message",
        "end",
        "$stdout.puts('" + MARKER + "' + JSON.generate(out))"
    );

    private BundlerProbe() {
    }

    @Nullable
    public static BundlerProbeResult parse(String output) {
        int index = output.lastIndexOf(MARKER);
        if (index < 0) {
            return null;
        }

        String json = output.substring(index + MARKER.length()).trim();
        int lineEnd = json.indexOf('\n');
        if (lineEnd >= 0) {
            json = json.substring(0, lineEnd);
        }

        JsonObject object;
        try {
            object = JsonParser.parseString(json).getAsJsonObject();
        }
        catch (RuntimeException e) {
            return null;
        }

        List<BundlerGem> gems = new ArrayList<>();
        JsonArray gemsArray = object.getAsJsonArray("gems");
        if (gemsArray != null) {
            for (JsonElement element : gemsArray) {
                JsonObject gem = element.getAsJsonObject();
                gems.add(new BundlerGem(getString(gem, "name"), getString(gem, "version"), getString(gem, "path"), getStrings(gem, "requirePaths")));
            }
        }

        return new BundlerProbeResult(getNullableString(object, "ruby"),
            getStrings(object, "dependencies"),
            gems,
            getNullableString(object, "missing"),
            getNullableString(object, "error"));
    }

    private static String getString(JsonObject object, String key) {
        String value = getNullableString(object, key);
        return value == null ? "" : value;
    }

    @Nullable
    private static String getNullableString(JsonObject object, String key) {
        JsonElement element = object.get(key);
        return element == null || element.isJsonNull() ? null : element.getAsString();
    }

    private static List<String> getStrings(JsonObject object, String key) {
        JsonArray array = object.getAsJsonArray(key);
        if (array == null) {
            return List.of();
        }
        List<String> result = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            result.add(element.getAsString());
        }
        return result;
    }
}
