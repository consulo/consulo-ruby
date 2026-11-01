package consulo.ruby.module.extension;

import consulo.content.bundle.SdkType;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.extension.ModuleExtensionWithSdkBase;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.settings.RSupportPerModuleSettingsImpl;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkType;
import org.jetbrains.plugins.ruby.settings.RSupportPerModuleSettings;

/**
 * @author VISTALL
 * @since 25.09.13.
 */
public class BaseRubyModuleExtension extends ModuleExtensionWithSdkBase<BaseRubyModuleExtension> implements RubyModuleExtension<BaseRubyModuleExtension>
{
	private RSupportPerModuleSettings mySettings;

	public BaseRubyModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer module)
	{
		super(id, module);

		mySettings = new RSupportPerModuleSettingsImpl();
	}

	@Nonnull
	@Override
	public Class<? extends SdkType> getSdkTypeClass()
	{
		return RubySdkType.class;
	}

	@Nonnull
	@Override
	public RSupportPerModuleSettings getSettings()
	{
		return mySettings;
	}
}
