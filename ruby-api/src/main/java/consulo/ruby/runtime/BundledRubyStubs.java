package consulo.ruby.runtime;

import consulo.container.plugin.PluginManager;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.StubsUrls;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkType;

import java.io.File;

/**
 * The core stubs shipped with the plugin, for runtimes the stubs are not generated for.
 */
public final class BundledRubyStubs {
    private BundledRubyStubs() {
    }

    public static String getDirectoryUrl() {
        return VirtualFileUtil.pathToUrl(new File(PluginManager.getPluginPath(RubySdkType.class), RubySdkType.RUBYSTUBS_DIR).getPath());
    }

    public static String getBuiltInUrl() {
        return getDirectoryUrl() + StubsUrls.BUILT_IN_RB;
    }
}
