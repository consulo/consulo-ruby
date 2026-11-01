package consulo.ruby.runtime;

import consulo.annotation.component.ExtensionImpl;
import consulo.content.bundle.Sdk;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.ruby.module.extension.RubyModuleExtension;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil;
import org.jspecify.annotations.Nullable;

/**
 * The Ruby SDK of a Ruby module.
 */
@ExtensionImpl(order = "last")
public class SdkRubyRuntimeProvider implements RubyRuntimeProvider {
    @Nullable
    @Override
    public RubyRuntime getRuntime(Module module) {
        RubyModuleExtension<?> extension = ModuleUtilCore.getExtension(module, RubyModuleExtension.class);
        Sdk sdk = extension != null ? extension.getSdk() : null;
        return RubySdkUtil.isKindOfRubySDK(sdk) ? new SdkRubyRuntime(sdk) : null;
    }
}
