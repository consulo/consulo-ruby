package consulo.jruby.impl.module.extension;

import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.extension.ModuleExtensionBase;
import jakarta.annotation.Nonnull;

/**
 * JRuby in a Java module: the JRuby jar comes with the module's dependencies, there is no JRuby SDK.
 */
public class JRubyModuleExtension extends ModuleExtensionBase<JRubyModuleExtension>
{
	public JRubyModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer moduleRootLayer)
	{
		super(id, moduleRootLayer);
	}
}
