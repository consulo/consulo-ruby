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

package org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.impl.module;

import consulo.ruby.runtime.RubyRuntime;
import consulo.ruby.runtime.RubyRuntimeProvider;
import consulo.module.content.layer.OrderEnumerator;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import consulo.content.bundle.Sdk;
import consulo.module.Module;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.CachedSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.FileSymbolType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.SymbolsCache;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.impl.AbstractLayeredCachedSymbol;
import org.jetbrains.plugins.ruby.settings.RSupportPerModuleSettings;
import org.jetbrains.plugins.ruby.support.ui.checkableDir.CheckableDirectoriesContainer;
import org.jetbrains.plugins.ruby.support.ui.checkableDir.CheckableDirectoryItem;
import org.jetbrains.plugins.ruby.support.utils.RModuleUtil;
import org.jetbrains.plugins.ruby.support.utils.VirtualFileUtil;
import consulo.project.Project;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Jan 25, 2008
 */
public class ModuleLayer extends AbstractLayeredCachedSymbol
{

	public ModuleLayer(@Nonnull final Project project, @Nullable final Module module, @Nullable final Sdk sdk, final boolean interopEnabled)
	{
		super(project, module, sdk, interopEnabled);
	}

	@Override
	@Nullable
	protected CachedSymbol getBaseSymbol()
	{
		final SymbolsCache cache = SymbolsCache.getInstance(myProject);
		return cache.getBuiltInCachedSymbol(FileSymbolType.BUILT_IN, mySdk, isInteropEnabled);
	}

	@Override
	protected void addAdditionalData()
	{
		// Extend loadpath with user directories
		if(myModule != null)
		{
			CheckableDirectoriesContainer checkDirectories = null;
			RSupportPerModuleSettings settings = RModuleUtil.getRubySupportSettings(myModule);
			if(settings != null)
			{
				checkDirectories = settings.getLoadPathDirs();
			}

			if(checkDirectories != null)
			{
				for(CheckableDirectoryItem item : checkDirectories.getCheckableDirectories())
				{
					if(item.isChecked())
					{
						myFileSymbol.addLoadPathUrl(VirtualFileUtil.constructLocalUrl(item.getDirectoryPath()));
					}
				}
			}

			// the load path of the runtime, e.g. a standard library inside a jar
			final RubyRuntime runtime = RubyRuntimeProvider.findRuntime(myModule);
			if(runtime != null)
			{
				for(String url : runtime.getLoadPathUrls())
				{
					myFileSymbol.addLoadPathUrl(url);
				}
			}

			// the libraries of the module - the gems of a Bundler project
			for(String url : OrderEnumerator.orderEntries(myModule).librariesOnly().recursively().sources().getUrls())
			{
				myFileSymbol.addLoadPathUrl(url);
			}
		}
	}

	@Override
	public void fileAdded(@Nonnull final String url)
	{
		// do nothing
	}
}
