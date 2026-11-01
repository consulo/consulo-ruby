package consulo.ruby.impl.bundler.resolve;

import java.util.List;

/**
 * A locked gem. {@code path} is empty and {@code requirePaths} too when the gem is not installed.
 *
 * @author VISTALL
 * @since 2026-11-01
 */
public record BundlerGem(String name, String version, String path, List<String> requirePaths) {
    public boolean isInstalled() {
        return !path.isEmpty();
    }
}
