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

import consulo.content.bundle.Sdk;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.rails.nameConventions.MailersConventions;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.FileSymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.InterpretationMode;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.RailsRequireUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.StubsUrls;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.SymbolCacheUtil;
import org.jetbrains.plugins.ruby.ruby.sdk.gemRootType.GemOrderRootType;
import consulo.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Oct 8, 2007
 */
public class RailsBuiltInCachedSymbol extends BuiltInCachedSymbol
{
	private static final Logger LOG = Logger.getInstance(RailsBuiltInCachedSymbol.class.getName());

	public RailsBuiltInCachedSymbol(@Nonnull final Project project, @Nonnull final String url, @Nullable final Sdk sdk)
	{
		super(project, url, sdk);
	}

	@Override
	public void fileAdded(@Nonnull final String url)
	{
		super.fileAdded(url);
		if(mySdk == null)
		{
			return;
		}
		// We recreate cache if something added to gems
		final String[] gemsRootUrls = mySdk.getRootProvider().getUrls(GemOrderRootType.ID);
		for(String gemsRootUrl : gemsRootUrls)
		{
			if(url.startsWith(gemsRootUrl))
			{
				myFileSymbol = null;
				return;
			}
		}
	}

	@Override
	protected void addAdditionalData()
	{
		super.addAdditionalData();

		// Core extensions
		RailsRequireUtil.loadCoreExtentions(myFileSymbol, InterpretationMode.FULL);

		// Active record
		RailsRequireUtil.loadDBAdapters(myFileSymbol, InterpretationMode.FULL);

		// Action View
		RailsRequireUtil.loadBuiltInHelpers(myFileSymbol, InterpretationMode.FULL);

		// Action Mailer
		MailersConventions.loadBuiltInGems(myFileSymbol, InterpretationMode.FULL);
	}
}
