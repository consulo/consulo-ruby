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

package org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure;

import jakarta.annotation.Nonnull;

import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.TypeSet;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Mar 3, 2008
 */
public class SymbolFilterFactory
{
	public static final SymbolFilter CLASSES_ONLY_FILTER = SymbolFilterFactory.createFilterByTypeSet(new TypeSet(Type.CLASS, Type.MODULE, Type.FOREIGN_CLASS, Type.FOREIGN_PROXY_CLASS, Type.FOREIGN_PACKAGE));

	public static final SymbolFilter EMPTY_FILTER = new SymbolFilter()
	{
		@Override
		public boolean accept(@Nonnull final Symbol symbol)
		{
			return true;
		}
	};


	public static SymbolFilter createFilterByTypeSet(final TypeSet typeSet)
	{
		return new SymbolFilter()
		{
			@Override
			public boolean accept(@Nonnull final Symbol symbol)
			{
				return typeSet.contains(symbol.getType());
			}
		};
	}

	public static SymbolFilter createFilterByNameAndTypeSet(@Nonnull final String name, final TypeSet typeSet)
	{
		return new SymbolFilter()
		{
			@Override
			public boolean accept(@Nonnull final Symbol symbol)
			{
				return typeSet.contains(symbol.getType()) && name.equals(symbol.getName());
			}
		};
	}

}
