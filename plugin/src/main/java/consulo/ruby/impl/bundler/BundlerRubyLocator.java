package consulo.ruby.impl.bundler;

import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.ruby.impl.bundler.setting.BundlerExecutionSettings;
import consulo.ruby.impl.bundler.setting.BundlerProjectSettings;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkType;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Picks the Ruby SDK of a Bundler project: the one of the project settings, else the one matching
 * {@code .ruby-version}, else the most recent Ruby SDK. Without any SDK the {@code ruby} of the PATH runs.
 *
 * @author VISTALL
 * @since 2026-11-01
 */
public final class BundlerRubyLocator {
    public static final String PATH_RUBY = "ruby";

    private BundlerRubyLocator() {
    }

    public static BundlerExecutionSettings createExecutionSettings(String projectPath, @Nullable BundlerProjectSettings projectSettings) {
        Sdk sdk = findSdk(BundlerPaths.getRootDir(projectPath), projectSettings == null ? null : projectSettings.getSdkName());
        if (sdk == null) {
            return new BundlerExecutionSettings(PATH_RUBY, null);
        }
        String executable = RubySdkUtil.getVMExecutablePath(sdk);
        return new BundlerExecutionSettings(executable == null ? PATH_RUBY : executable, sdk.getName());
    }

    @Nullable
    public static Sdk findSdk(Path rootDir, @Nullable String sdkName) {
        SdkTable sdkTable = SdkTable.getInstance();
        if (sdkName != null) {
            Sdk sdk = sdkTable.findSdk(sdkName);
            if (sdk != null) {
                return sdk;
            }
        }

        List<Sdk> sdks = sdkTable.getSdksOfType(RubySdkType.getInstance());
        String requestedVersion = readRubyVersion(rootDir);
        if (requestedVersion != null) {
            for (Sdk sdk : sdks) {
                String versionString = sdk.getVersionString();
                if (versionString != null && versionString.contains(requestedVersion)) {
                    return sdk;
                }
            }
        }
        return sdkTable.findMostRecentSdkOfType(RubySdkType.getInstance());
    }

    @Nullable
    public static String readRubyVersion(Path rootDir) {
        Path file = rootDir.resolve(BundlerConstants.RUBY_VERSION_FILE);
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try {
            String version = Files.readString(file).trim();
            if (version.startsWith("ruby-")) {
                version = version.substring("ruby-".length());
            }
            return version.isEmpty() ? null : version;
        }
        catch (IOException e) {
            return null;
        }
    }
}
