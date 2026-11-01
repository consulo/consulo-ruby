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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import consulo.content.bundle.Sdk;
import consulo.module.Module;
import org.jetbrains.plugins.ruby.rails.facet.RailsFacetUtil;
import org.jetbrains.plugins.ruby.rails.facet.configuration.StandardRailsPaths;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.RailsRequireUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.CachedSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.FileSymbolType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.SymbolsCache;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.impl.AbstractCachedSymbol;
import consulo.project.Project;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Nov 12, 2007
 */
public class RailsModuleLayer extends ModuleLayer
{
	private final String myLayerRootUrl;

	public RailsModuleLayer(@Nonnull final Project project, @Nullable final Module module, @Nullable final Sdk sdk, final boolean interopEnabled)
	{
		super(project, module, sdk, interopEnabled);
		assert module != null;
		final StandardRailsPaths railsPaths = RailsFacetUtil.getRailsAppPaths(module);
		assert railsPaths != null; //Not null for modules with Rails Support
		myLayerRootUrl = railsPaths.getVendorRootURL();
	}

	@Override
	public void fileAdded(@Nonnull final String url)
	{
		if(url.startsWith(myLayerRootUrl))
		{
			myFileSymbol = null;
			return;
		}
		final CachedSymbol baseCachedSymbol = getBaseSymbol();
		if(baseCachedSymbol != null)
		{
			((AbstractCachedSymbol) baseCachedSymbol).fileAdded(url);
			if(!baseCachedSymbol.isUp2Date())
			{
				myFileSymbol = null;
			}
		}
	}

	@Override
	@SuppressWarnings({"StringEquality"})
	@Nullable
	protected CachedSymbol getBaseSymbol()
	{
		final SymbolsCache cache = SymbolsCache.getInstance(myProject);
		return cache.getBuiltInCachedSymbol(FileSymbolType.RAILS_BUILT_IN, mySdk, isInteropEnabled);
	}

	@Override
	protected void addAdditionalData()
	{
		super.addAdditionalData();

		// add default rails loadpath
		RailsRequireUtil.addRailsLoadPath(myFileSymbol, myModule);
	}
}
