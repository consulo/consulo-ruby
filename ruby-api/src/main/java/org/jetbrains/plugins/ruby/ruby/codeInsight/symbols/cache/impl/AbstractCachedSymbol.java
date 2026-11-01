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

import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import consulo.application.progress.ProgressManager;
import consulo.module.Module;
import consulo.project.Project;
import consulo.util.lang.ref.SoftReference;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.CacheKey;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.CachedSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import consulo.content.bundle.Sdk;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Oct 7, 2007
 */
public abstract class AbstractCachedSymbol implements CachedSymbol
{
	// full file symbol
	protected FileSymbol myFileSymbol;
	protected Module myModule;
	protected Sdk mySdk;
	protected Project myProject;

	private CacheKey myKey;

	public AbstractCachedSymbol(@Nonnull final Project project, @Nullable final Module module, @Nullable final Sdk sdk)
	{
		myProject = project;
		myModule = module;
		mySdk = sdk;
	}

	public final void fileRemoved(@Nonnull String url)
	{
		fileChanged(url);
	}

	public final void fileUpdated(@Nonnull String url)
	{
		fileChanged(url);
	}

	public abstract void fileAdded(@Nonnull String url);

	protected abstract void fileChanged(@Nonnull String url);

	@Override
	public final boolean isUp2Date()
	{
		return myFileSymbol != null;
	}

	@Override
	@Nullable
	public final FileSymbol getUp2DateSymbol()
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		if(!isUp2Date())
		{
			updateFileSymbol();
		}
		return myFileSymbol;
	}

	protected abstract void updateFileSymbol();


	public final void setKey(@Nonnull final CacheKey key)
	{
		myKey = key;
	}

}
