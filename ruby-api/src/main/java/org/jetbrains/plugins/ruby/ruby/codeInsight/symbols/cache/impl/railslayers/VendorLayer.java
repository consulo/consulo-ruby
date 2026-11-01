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

package org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.impl.railslayers;

import consulo.content.bundle.Sdk;
import consulo.module.Module;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.RailsRequireUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.CachedSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.FileSymbolType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.impl.AbstractCachedSymbol;
import consulo.project.Project;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Oct 20, 2007
 */

public class VendorLayer extends AbstractRailsLayeredCachedSymbol
{

	public VendorLayer(@Nonnull final Project project, @Nullable final Module module, @Nullable final Sdk sdk, final boolean isInteropEnabled)
	{
		super(FileSymbolType.RAILS_MODULE_LAYER, project, module, sdk, isInteropEnabled);
	}

	@Override
	public void fileAdded(@Nonnull String url)
	{
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
	protected void addAdditionalData()
	{
		RailsRequireUtil.loadAllVendorsLibsWithoutEdgeRails(myFileSymbol, myModule);
	}

}
