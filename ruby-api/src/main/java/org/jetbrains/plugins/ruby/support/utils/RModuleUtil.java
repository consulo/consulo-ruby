/*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.ruby.support.utils;

import java.util.LinkedList;
import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import consulo.language.util.ModuleUtilCore;
import consulo.module.ModuleManager;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFileManager;
import org.jetbrains.plugins.ruby.settings.RSupportPerModuleSettings;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.content.bundle.Sdk;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.content.ModuleRootManager;
import consulo.virtualFileSystem.VirtualFile;
import consulo.util.lang.function.ThrowableRunnable;
import consulo.ruby.module.extension.RubyModuleExtension;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg, Roman.Chernyatchik
 * @date: Sep 29, 2006
 */
public class RModuleUtil
{
	private static final Logger LOG = Logger.getInstance(RModuleUtil.class.getName());

	/**
	 * Returns all the modules with Ruby support in given project
	 *
	 * @param project Project to search modules in
	 * @return Array of found modules
	 */
	public static Module[] getAllModulesWithRubySupport(@Nonnull final Project project)
	{
		final List<Module> result = new LinkedList<Module>();

		final Module[] modules = ModuleManager.getInstance(project).getModules();
		for(Module module : modules)
		{
			if(hasRubySupport(module))
			{
				result.add(module);
			}
		}
		return result.toArray(new Module[result.size()]);
	}

	/**
	 * @param module some module
	 * @return True if module has Ruby support
	 */
	public static boolean hasRubySupport(@Nullable final Module module)
	{
		return module != null && ModuleUtilCore.getExtension(module, RubyModuleExtension.class) != null;
	}

	/**
	 * @param module Module to get JDK for
	 * @return Ruby SDK selected for given module
	 */
	@Nullable
	public static Sdk getModuleSdk(@Nullable final Module module)
	{
		if(module == null)
		{
			return null;
		}
		RubyModuleExtension extension = ModuleUtilCore.getExtension(module, RubyModuleExtension.class);
		return extension != null ? extension.getSdk() : null;
	}

	/**
	 * @param module Module to get content root
	 * @return VirtualFile corresponding to content root
	 */
	@Nullable
	public static VirtualFile getRubyModuleTypeRoot(@Nonnull final Module module)
	{
		final VirtualFile[] roots = ModuleRootManager.getInstance(module).getContentRoots();
		return roots.length > 0 ? roots[0] : null;
	}

	/**
	 * @param rootModel Corresponding for Rails module root model
	 * @return VirtualFile corresponding to content root
	 */
	@Nullable
	public static VirtualFile getModulesFirstContentRoot(@Nonnull final ModifiableRootModel rootModel)
	{
		final VirtualFile[] roots = rootModel.getContentRoots();
		return roots.length > 0 ? roots[0] : null;
	}

	/**
	 * Refreshes(in the write action) the cached file system information for ruby module
	 * from the physical file system.
	 *
	 * @param module Ruby Module Type.
	 */
	public static void refreshRubyModuleTypeContent(final Module module)
	{
		IdeaInternalUtil.runInsideWriteAction(new consulo.util.lang.function.ThrowableRunnable<Exception>()
		{
			@Override
			public void run() throws Exception
			{
				final VirtualFile moduleRoot = RModuleUtil.getRubyModuleTypeRoot(module);
				if(moduleRoot != null)
				{
					moduleRoot.refresh(false, true);
				}
			}
		});
	}

	public static Runnable createWriteAction(final Runnable action)
	{
		return new Runnable()
		{
			@Override
			public void run()
			{
				IdeaInternalUtil.runInsideWriteAction(new ThrowableRunnable<Exception>()
				{
					@Override
					public void run() throws Exception
					{
						action.run();
					}
				});
			}
		};
	}

	/**
	 * Refreshes(in a separate thread) the cached file system information
	 * from the physical file system.
	 */
	public static void synchronizeFSChanged()
	{
		IdeaInternalUtil.runInsideWriteAction(new ThrowableRunnable<Exception>()
		{
			@Override
			public void run() throws Exception
			{
				VirtualFileManager.getInstance().asyncRefresh(null);
			}
		});
	}

	/**
	 * @param module Some module
	 * @return If module is Ruby module returns it's settings, otherwize nil
	 */
	@Nullable
	public static RSupportPerModuleSettings getRubySupportSettings(@Nonnull final Module module)
	{
		RubyModuleExtension extension = ModuleUtilCore.getExtension(module, RubyModuleExtension.class);
		if(extension == null)
		{
			return null;
		}

		return extension.getSettings();
	}
}
