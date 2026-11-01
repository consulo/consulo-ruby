package consulo.jruby.impl.runtime;

import com.intellij.java.language.projectRoots.JavaSdkType;
import consulo.annotation.component.ExtensionImpl;
import consulo.content.bundle.Sdk;
import consulo.java.language.module.extension.JavaModuleExtension;
import consulo.jruby.impl.module.extension.JRubyModuleExtension;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.content.layer.OrderEnumerator;
import consulo.ruby.runtime.RubyRuntime;
import consulo.ruby.runtime.RubyRuntimeProvider;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@ExtensionImpl
public class JRubyRuntimeProvider implements RubyRuntimeProvider {
    private static final String STDLIB_PATH = "META-INF/jruby.home/lib/ruby/stdlib";

    @Nullable
    @Override
    public RubyRuntime getRuntime(Module module) {
        if (ModuleUtilCore.getExtension(module, JRubyModuleExtension.class) == null) {
            return null;
        }
        JavaModuleExtension<?> javaExtension = ModuleUtilCore.getExtension(module, JavaModuleExtension.class);
        Sdk sdk = javaExtension == null ? null : javaExtension.getSdk();
        if (sdk == null || !(sdk.getSdkType() instanceof JavaSdkType javaSdkType)) {
            return null;
        }
        String binPath = javaSdkType.getBinPath(sdk);
        if (binPath == null) {
            return null;
        }

        OrderEnumerator runtimeEntries = OrderEnumerator.orderEntries(module).runtimeOnly().recursively().withoutSdk();
        List<String> loadPathUrls = new ArrayList<>();
        for (VirtualFile root : runtimeEntries.getClassesRoots()) {
            VirtualFile stdlib = root.findFileByRelativePath(STDLIB_PATH);
            if (stdlib != null && stdlib.isDirectory()) {
                loadPathUrls.add(stdlib.getUrl());
            }
        }
        return new JRubyRuntime(binPath + File.separator + "java", runtimeEntries.getPathsList().getPathsString(), loadPathUrls);
    }
}
