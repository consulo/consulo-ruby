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

package org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.impl.builtin;

import consulo.application.ReadAction;
import jakarta.annotation.Nullable;

import consulo.application.ApplicationManager;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.ProgressManager;
import consulo.content.bundle.Sdk;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.FileSymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.InterpretationMode;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.CachedSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.impl.AbstractLayeredCachedSymbol;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil;
import consulo.logging.Logger;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Oct 8, 2007
 */
public class BuiltInCachedSymbol extends AbstractLayeredCachedSymbol
{
	protected String myLoadUrl;
	private static final Logger LOG = Logger.getInstance(BuiltInCachedSymbol.class.getName());

	public BuiltInCachedSymbol(@Nonnull final Project project, @Nonnull final String url, @Nullable final Sdk sdk)
	{
		super(project, null, sdk, false);
		myLoadUrl = url;
	}

	/**
	 * Rebuilds FileSymbol with ProgressBar
	 */
	@Override
	protected final void updateFileSymbol()
	{
		if(myFileSymbol == null)
		{
			final ProgressManager manager = ProgressManager.getInstance();

			final Runnable runnable = new Runnable()
			{
				@Override
				public void run()
				{
					final ProgressIndicator indicator = manager.getProgressIndicator();
					if(indicator != null && mySdk != null)
					{
						indicator.setText(mySdk.getName());
					}
                    ReadAction.run(() -> BuiltInCachedSymbol.super.updateFileSymbol());
				}
			};
			// built in the thread that asks, under its progress if any - never a modal dialog
			runnable.run();
		}
	}

	@Override
	@Nullable
	protected CachedSymbol getBaseSymbol()
	{
		return null;
	}

	@Override
	public void fileAdded(@Nonnull String url)
	{
		if(mySdk != null)
		{
			if(url.startsWith(RubySdkUtil.getRubyStubsDirUrl(mySdk)))
			{
				myFileSymbol = null;
			}
		}
	}

	private void addSdkLoadPath()
	{
		if(mySdk != null && RubySdkUtil.isKindOfRubySDK(mySdk))
		{
			for(String rootUrl : RubySdkUtil.getSdkRootsWithAllGems(mySdk))
			{
				// TODO: uncomment, when require_gem and gem commands will be supported, load all the gems into loadpath
				//                        // Add rails gems to loadpath
				//                        for (String gemLibUrl : RailsGemsUtil.getRailsGems(mySdk)) {
				//                            myFileSymbol.addLoadPath(gemLibUrl);
				//                        }
				FileSymbolUtil.addLoadPath(myFileSymbol, rootUrl);
			}
		}
	}

	@Override
	protected void addAdditionalData()
	{
		addSdkLoadPath();
		if(!ApplicationManager.getApplication().isUnitTestMode())
		{
			if(VirtualFileManager.getInstance().findFileByUrl(myLoadUrl) == null)
			{
				// the stubs of an updated plugin are not in the VFS yet - the next cache update sees them
				final int slash = myLoadUrl.lastIndexOf('/');
				final VirtualFile stubsDirectory = slash > 0 ? VirtualFileManager.getInstance().findFileByUrl(myLoadUrl.substring(0, slash)) : null;
				if(stubsDirectory != null)
				{
					stubsDirectory.refresh(true, false);
				}
				LOG.warn("Stubs are not found: " + myLoadUrl);
			}
		}
		FileSymbolUtil.process(myFileSymbol, myLoadUrl, InterpretationMode.FULL, true);
	}
}
