package consulo.ruby.impl.bundler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
public final class BundlerPaths {
    private BundlerPaths() {
    }

    public static Path getRootDir(String projectPath) {
        Path path = Path.of(projectPath).toAbsolutePath().normalize();
        if (Files.isRegularFile(path)) {
            Path parent = path.getParent();
            return parent == null ? path : parent;
        }
        return path;
    }

    public static boolean isConfigFileName(String fileName) {
        return BundlerConstants.GEMFILE.equals(fileName)
            || BundlerConstants.GEMFILE_LOCK.equals(fileName)
            || BundlerConstants.RUBY_VERSION_FILE.equals(fileName)
            || fileName.endsWith(BundlerConstants.GEMSPEC_EXTENSION);
    }

    public static List<Path> getConfigFiles(Path rootDir) {
        List<Path> result = new ArrayList<>();
        result.add(rootDir.resolve(BundlerConstants.GEMFILE));
        result.add(rootDir.resolve(BundlerConstants.GEMFILE_LOCK));
        result.add(rootDir.resolve(BundlerConstants.RUBY_VERSION_FILE));
        try (Stream<Path> children = Files.list(rootDir)) {
            children.filter(child -> child.getFileName().toString().endsWith(BundlerConstants.GEMSPEC_EXTENSION)).forEach(result::add);
        }
        catch (Exception ignored) {
        }
        return result;
    }
}
