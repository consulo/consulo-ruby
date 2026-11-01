package org.jetbrains.plugins.ruby.rails.facet;

import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.util.dataholder.Key;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.rails.facet.configuration.BaseRailsFacetConfiguration;
import org.jetbrains.plugins.ruby.rails.facet.configuration.BaseRailsFacetConfigurationImpl;
import org.jetbrains.plugins.ruby.support.utils.RModuleUtil;

/**
 * Rails support of a module: the {@code ruby-on-rails} module extension (the Bundler import enables it when
 * {@code railties} is locked) and the Rails application found in the content roots.
 *
 * @author VISTALL
 * @since 2026-11-01
 */
public final class RailsModuleSupport
{
	public static final String EXTENSION_ID = "ruby-on-rails";

	private static final Key<BaseRailsFacetConfigurationImpl> CONFIGURATION = Key.create("RailsModuleSupport.CONFIGURATION");

	private RailsModuleSupport()
	{
	}

	public static boolean isEnabled(@Nullable final Module module)
	{
		return module != null && !module.isDisposed() && ModuleUtilCore.getExtension(module, EXTENSION_ID) != null;
	}

	@Nullable
	public static BaseRailsFacetConfiguration getConfiguration(@Nonnull final Module module)
	{
		if(!isEnabled(module))
		{
			return null;
		}

		final String rootPath = findApplicationRoot(module);
		if(rootPath == null)
		{
			return null;
		}

		BaseRailsFacetConfigurationImpl configuration = module.getUserData(CONFIGURATION);
		if(configuration != null && rootPath.equals(configuration.getNullableRailsApplicationRootPath()))
		{
			return configuration;
		}

		configuration = new BaseRailsFacetConfigurationImpl();
		configuration.setRailsApplicationRootPath(rootPath);
		configuration.setModule(module);
		configuration.setRailsApplicationRootPath(rootPath);
		configuration.setSdk(RModuleUtil.getModuleSdk(module));
		configuration.setInitialized();
		module.putUserData(CONFIGURATION, configuration);
		return configuration;
	}

	/**
	 * @return the content root with {@code config/application.rb} (Rails 3+) or {@code config/boot.rb}, else the first one
	 */
	@Nullable
	private static String findApplicationRoot(@Nonnull final Module module)
	{
		final VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
		for(VirtualFile contentRoot : contentRoots)
		{
			if(contentRoot.findFileByRelativePath("config/application.rb") != null || contentRoot.findFileByRelativePath("config/boot.rb") != null)
			{
				return contentRoot.getPath();
			}
		}
		return contentRoots.length > 0 ? contentRoots[0].getPath() : null;
	}
}
