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

package org.jetbrains.plugins.ruby.ruby.gotoByName;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.function.Processor;
import consulo.content.scope.SearchScope;
import consulo.ide.navigation.GotoSymbolContributor;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.FindSymbolParameters;
import consulo.language.psi.stub.IdFilter;
import consulo.language.psi.stub.StubIndex;
import consulo.navigation.NavigationItem;
import consulo.project.content.scope.ProjectAwareSearchScope;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.stubs.index.RubyClassNameIndex;
import org.jetbrains.plugins.ruby.ruby.lang.psi.stubs.index.RubyMethodNameIndex;
import org.jetbrains.plugins.ruby.ruby.lang.psi.stubs.index.RubyModuleNameIndex;

import java.util.ArrayList;
import java.util.List;

/**
 * Go to Symbol: classes, modules and methods
 */
@ExtensionImpl
public class RubySymbolContributor extends RubyBaseContributor implements GotoSymbolContributor
{
	@Override
	public void processNames(Processor<String> processor, SearchScope searchScope, IdFilter idFilter)
	{
		ProjectAwareSearchScope scope = (ProjectAwareSearchScope) searchScope;
		StubIndex.getInstance().processAllKeys(RubyClassNameIndex.KEY, processor, scope, idFilter);
		StubIndex.getInstance().processAllKeys(RubyModuleNameIndex.KEY, processor, scope, idFilter);
		StubIndex.getInstance().processAllKeys(RubyMethodNameIndex.KEY, processor, scope, idFilter);
	}

	@Override
	public void processElementsWithName(String name, Processor<NavigationItem> processor, FindSymbolParameters parameters)
	{
		GlobalSearchScope scope = parameters.isSearchInLibraries() ? GlobalSearchScope.allScope(parameters.getProject()) : GlobalSearchScope.projectScope(parameters.getProject());
		List<RPsiElement> elements = new ArrayList<>();
		elements.addAll(RubyClassNameIndex.find(name, parameters.getProject(), scope));
		elements.addAll(RubyModuleNameIndex.find(name, parameters.getProject(), scope));
		elements.addAll(RubyMethodNameIndex.find(name, parameters.getProject(), scope));
		for(NavigationItem item : getItems(elements, parameters.getProject()))
		{
			if(!processor.process(item))
			{
				return;
			}
		}
	}
}
