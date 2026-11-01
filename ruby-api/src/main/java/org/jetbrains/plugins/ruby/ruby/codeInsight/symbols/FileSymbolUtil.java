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

package org.jetbrains.plugins.ruby.ruby.codeInsight.symbols;

import consulo.application.progress.ProgressManager;
import consulo.content.bundle.Sdk;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.FileSymbolType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.SymbolCacheUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.SymbolsCache;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.support.utils.RModuleUtil;

import java.util.Collections;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: May 11, 2007
 */
public class FileSymbolUtil
{
	private static final Logger LOG = Logger.getInstance(FileSymbolUtil.class.getName());

	/**
	 * Returns the full symbol for file.
	 * It is macros for getFileSymbol(rFile, isLightRubyTestMode) where isLightRubyTestMode is false.
	 *
	 * @param rFile Ruby file to get symbol for
	 * @return The symbol - the lineared sum of file and all required objects
	 */
	@Nullable
	public static FileSymbol getFileSymbol(@Nonnull final RFile rFile)
	{
		return getFileSymbol(rFile, false);
	}

	/**
	 * Returns the full symbol for file
	 *
	 * @param rVFile         Pure virtual file to get symbol for(psi will be replaced with it's virtual)
	 * @param isRubyTestMode True if symbol should be evaluated for ruby test mode,
	 *                       in this mode symbol builder process only requires to files form tests folders
	 * @return The symbol - the lineared sum of file and all required objects
	 */
	@Nullable
	public static FileSymbol getFileSymbol(@Nonnull final RFile rVFile, final boolean isRubyTestMode)
	{
		// Check if this action is not stopped
		ProgressManager.getInstance().checkCanceled();
		final RFile rFile = rVFile.getOriginalFile() instanceof RFile originalFile ? originalFile : rVFile;
		final RFile pureRVFile = rVFile;

		final VirtualFile file = rFile.getVirtualFile();
		if(file == null)
		{
			return null;
		}

		final Module module = rFile.getModule();
		final Project project = rFile.getProject();
		final boolean interopEnabled = rFile.isInteropEnabled();
		final String url = file.getUrl();

		// Symbol build should work with pure virtual files
		// because it incorrectly works on psi files
		if(pureRVFile == null)
		{
			// can't get from cache in not ruby projects
			return null;
		}

		if(isRubyTestMode)
		{
			final FileSymbol fileSymbol = new FileSymbol(null, project, interopEnabled);
			FileSymbolUtil.process(fileSymbol, url, InterpretationMode.ONLY_TESTS_EXTERNAL, true);
			return fileSymbol;
		}

		//noinspection ConstantConditions
		final Sdk sdk = module != null ? RModuleUtil.getModuleSdk(module) : null;

		final FileSymbol fileSymbol = SymbolCacheUtil.getFileSymbol(SymbolsCache.getInstance(project).getModifiableCachedSymbol(FileSymbolType.MODIFIABLE, url, module, sdk, interopEnabled));
		// setting last evaluated symbol
		LastSymbolStorage.getInstance(project).setSymbol(fileSymbol);
		return fileSymbol;
	}

	// Nullable safe operations
	public static void addLoadPath(@Nullable final FileSymbol fileSymbol, @Nonnull final String url)
	{
		if(fileSymbol != null)
		{
			fileSymbol.addLoadPathUrl(url);
		}
	}

	public static void process(@Nullable final FileSymbol fileSymbol, @Nonnull final String url, @Nonnull final InterpretationMode mode, final boolean forceAdd)
	{
		if(fileSymbol != null)
		{
			fileSymbol.process(url, mode, forceAdd);
		}
	}

	@Nonnull
	public static Set<String> getUrls(@Nullable final FileSymbol fileSymbol)
	{
		if(fileSymbol != null)
		{
			return fileSymbol.getUrls();
		}
		return Collections.emptySet();
	}
}
