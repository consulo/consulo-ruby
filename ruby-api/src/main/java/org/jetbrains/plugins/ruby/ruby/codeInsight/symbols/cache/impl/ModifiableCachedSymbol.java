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

package org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.impl;

import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;

import java.util.List;
import java.util.Set;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import consulo.content.bundle.Sdk;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import org.jetbrains.plugins.ruby.rails.facet.RailsFacetUtil;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RRequire;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.FileSymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.InterpretationMode;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.RailsRequireUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.CachedSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.FileSymbolType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.SymbolCacheUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.SymbolsCache;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil;
import consulo.module.Module;
import consulo.project.Project;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Oct 7, 2007
 */
public class ModifiableCachedSymbol extends AbstractCachedSymbol
{
	// Requires from main file (URLs gathered from require/load calls)
	private List<String> myRequires;

	// All the list of required files
	private Set<String> myAllExternalUrls;

	private FileSymbol myOuterSymbol;
	private String myUrl;
	private boolean isInteropEnabled;

	public ModifiableCachedSymbol(@Nonnull final Project project, @Nonnull final String url, @Nullable final Module module, @Nullable final Sdk sdk, final boolean interopEnabled)
	{
		super(project, module, sdk);
		myUrl = url;
		isInteropEnabled = interopEnabled;
	}

	@Override
	public void fileAdded(@Nonnull final String url)
	{
		// In common case, we clear cache if file is added
		myOuterSymbol = null;
		myFileSymbol = null;
	}

	@Override
	protected void fileChanged(@Nonnull final String url)
	{
		if(myOuterSymbol == null)
		{
			return;
		}

		final RFile file = resolveRFile();
		if(file == null)
		{
			return;
		}

		if(myUrl.equals(url))
		{
			myFileSymbol = null;
			if(myRequires == null || !myRequires.equals(file.getRequiredUrls()))
			{
				myOuterSymbol = null;
			}
			return;
		}

		if(myAllExternalUrls == null || myAllExternalUrls.contains(url))
		{
			myFileSymbol = null;
			myOuterSymbol = null;
		}
	}

	private void updateOuterSymbol(@Nonnull final RFile file)
	{
		if(myOuterSymbol != null)
		{
			return;
		}
		myOuterSymbol = new FileSymbol(SymbolCacheUtil.getFileSymbol(getBaseSymbol()), myProject, isInteropEnabled);
		FileSymbolUtil.process(myOuterSymbol, myUrl, InterpretationMode.EXTERNAL, false);

		// Adding rails specified symbol if needed
		if(myModule != null && RailsFacetUtil.hasRailsSupport(myModule))
		{
			RailsRequireUtil.requireRailsFiles(myOuterSymbol, file, myUrl, myModule);
		}
	}

	@Override
	protected void updateFileSymbol()
	{
		final RFile file = resolveRFile();
		if(file == null)
		{
			myFileSymbol = null;
			myOuterSymbol = null;
			return;
		}

		updateOuterSymbol(file);

		if(myFileSymbol == null)
		{
			myFileSymbol = new FileSymbol(myOuterSymbol, myProject, isInteropEnabled);
			FileSymbolUtil.process(myFileSymbol, myUrl, InterpretationMode.IGNORE_EXTERNAL, true);
			myAllExternalUrls = FileSymbolUtil.getUrls(myFileSymbol);
			myRequires = file.getRequiredUrls();
		}
	}

	@Nullable
	private RFile resolveRFile()
	{
		final VirtualFile vFile = VirtualFileManager.getInstance().findFileByUrl(myUrl);
		if(vFile == null)
		{
			return null;
		}
		final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(vFile);
		return psiFile instanceof RFile ? (RFile) psiFile : null;
	}

	@Nullable
	private CachedSymbol getBaseSymbol()
	{
		final RFile file = resolveRFile();
		if(file == null)
		{
			return null;
		}
		// a module without a Ruby SDK, e.g. one running JRuby from its dependencies, takes the bundled core stubs
		if(RubySdkUtil.isKindOfRubySDK(mySdk) || mySdk == null && myModule != null)
		{
			if(mySdk != null && RubySdkUtil.getRubyStubsDirUrl(mySdk) == null)
			{
				return null;
			}

			final SymbolsCache cache = SymbolsCache.getInstance(myProject);

			// Check if we`re inside rails module
			if(myModule != null && RailsFacetUtil.hasRailsSupport(myModule))
			{
				// Try to get level
				final FileSymbolType railsLayerType = RailsRequireUtil.getRailsLayerType(file, myModule);
				if(railsLayerType != null)
				{
					return cache.getCachedSymbol(railsLayerType, myModule, mySdk, isInteropEnabled);
				}
				// return rails module layer
				return cache.getCachedSymbol(FileSymbolType.RAILS_MODULE_LAYER, myModule, mySdk, isInteropEnabled);
			}
			// Else we return just module layer
			return cache.getCachedSymbol(FileSymbolType.MODULE_LAYER, myModule, mySdk, isInteropEnabled);
		}
		return null;
	}

}
