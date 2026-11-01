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

package org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.scope;

import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolFilter;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolFilterFactory;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Mar 3, 2008
 */
public class ClassesOnlyAutocompleteFilter implements ScopeAutocompleteFilter
{

	@Override
	public boolean allowForeign()
	{
		return false;
	}

	@Override
	public boolean allowOutterMembers()
	{
		return true;
	}

	@Override
	public boolean allowOwnMembers()
	{
		return true;
	}

	@Override
	public boolean allowLocalVariablesAndParameters()
	{
		return false;
	}

	@Override
	public boolean allowReswords()
	{
		return false;
	}

	@Override
	@Nonnull
	public SymbolFilter getSymbolFilter()
	{
		return SymbolFilterFactory.CLASSES_ONLY_FILTER;
	}
}