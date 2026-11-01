package consulo.ruby.runtime;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.application.Application;
import consulo.module.Module;
import org.jspecify.annotations.Nullable;

/**
 * Gives the runtime of the modules it knows. The Ruby SDK of a module is the last resort.
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface RubyRuntimeProvider {
    /**
     * @return the runtime of the module, null if the provider does not serve it
     */
    @Nullable
    RubyRuntime getRuntime(Module module);

    /**
     * @return the runtime of the first provider serving the module
     */
    @Nullable
    static RubyRuntime findRuntime(@Nullable Module module) {
        if (module == null || module.isDisposed()) {
            return null;
        }
        return Application.get().getExtensionPoint(RubyRuntimeProvider.class).computeSafeIfAny(provider -> provider.getRuntime(module));
    }
}
