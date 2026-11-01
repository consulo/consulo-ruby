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

import consulo.language.ast.IElementType;
import consulo.language.editor.completion.lookup.LookupValueWithPriority;
import consulo.language.psi.PsiElement;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import consulo.ruby.interop.RubyInterop;
import org.jetbrains.plugins.ruby.ruby.RubyIcons;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.ForeignLookupItem;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubyLookupItem;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubyPsiLookupItem;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubySimpleLookupItem;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.RailsSymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Types;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.Context;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RTypeUtil;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.bnf.BNF;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;
import org.jetbrains.plugins.ruby.ruby.presentation.SymbolPresentationUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Apr 22, 2007
 */
public class ScopeSymbolsUtil
{
	private static ArrayList<RubyLookupItem> RESWORDS_LOOKUP_ITEMS;

	/**
	 * Creates lookup Items for file
	 *
	 * @param fileSymbol FileSymbol
	 * @param element    anchor RPsiElement
	 * @param filter     Filter used
	 * @return List of RubyLookupItems
	 *         Priority:
	 *         0. foreign packages and classes
	 *         1. Outter members (constants, modules, classes only)
	 *         2. Superclasses and mixins members
	 *         3. Own members
	 *         4. local variables and parameters
	 *         5. Ruby reswords
	 */
	public static List<RubyLookupItem> getScopeSymbolsAndKeywordsLookupItems(@Nullable final FileSymbol fileSymbol, @Nonnull final RPsiElement element, @Nonnull final ScopeAutocompleteFilter filter)
	{
		final RContainer container = element instanceof RContainer ? (RContainer) element : element.getParentContainer();
		assert container != null;

		final HashMap<String, RubyLookupItem> map = new HashMap<String, RubyLookupItem>();

		if(fileSymbol != null)
		{
			// top level foreign packages and classes
			if(fileSymbol.isInteropEnabled() && filter.allowForeign())
			{
				RubyInterop.forEach(provider -> {
					for(PsiElement psiElement : provider.getTopLevelPackagesAndClasses(container.getProject()))
					{
						final RubyLookupItem item = new ForeignLookupItem(psiElement);
						map.put(item.getName(), item);
					}
				});
			}

			final Symbol context = SymbolUtil.getClassModuleFileSymbol(SymbolUtil.getSymbolByContainer(fileSymbol, container));
			if(filter.allowOutterMembers() && context != null)
			{
				// outter symbols
				Symbol anchor = context.getParentSymbol();
				while(anchor != null)
				{
					for(Symbol symbol : anchor.getChildren(fileSymbol).getSymbolsOfTypes(Types.MODULE_OR_CLASS_OR_CONSTANT).getChildrenByFilter(filter.getSymbolFilter()).getAll())
					{
						final String name = symbol.getName();
						if(name != null)
						{
							map.put(name, SymbolPresentationUtil.createRubyLookupItem(symbol, name, false, false));
						}
					}
					anchor = anchor.getParentSymbol();
				}


				// If we can add members from scope
				if(filter.allowOwnMembers())
				{

					// rails specific symbols
					for(Symbol symbol : RailsSymbolUtil.getRailsSpecificSymbols(fileSymbol, context, Context.ALL).getChildrenByFilter(filter.getSymbolFilter()).getAll())
					{
						final String name = symbol.getName();
						if(name != null)
						{
							final RubyLookupItem item = SymbolPresentationUtil.createRubyLookupItem(symbol, name, false, false);
							if(item != null)
							{
								map.put(item.getName(), item);
							}
						}
					}

					// symbols from scope
					final RType type = RTypeUtil.createTypeBySymbol(fileSymbol, context, Context.ALL, false);
					for(RubyLookupItem item : RTypeUtil.getLookupItemsByType(type, null, filter.getSymbolFilter()))
					{
						if(item != null)
						{
							// To prevent show java with unknown icon in autocompletion, except java package
							final String s = item.getName();
							if(!"java".equals(s))
							{
								map.put(s, item);
							}
						}
					}
				}
			}
		}

		// local variables
		if(filter.allowLocalVariablesAndParameters())
		{
			for(RubyLookupItem item : getScopeLocalVariables(element))
			{
				map.put(item.getName(), item);
			}
		}

		// reswords adding
		if(filter.allowReswords())
		{
			for(RubyLookupItem item : getReswordsLookupItems())
			{
				map.put(item.getName(), item);
			}
		}

		return new ArrayList<RubyLookupItem>(map.values());
	}

	private static ArrayList<RubyLookupItem> getReswordsLookupItems()
	{
		if(RESWORDS_LOOKUP_ITEMS == null)
		{
			final IElementType keywords[] = BNF.kRESWORDS.getTypes();

			final ArrayList<RubyLookupItem> variants = new ArrayList<RubyLookupItem>();
			for(IElementType type : keywords)
			{
				final String name = type.toString();
				final RubySimpleLookupItem lookupItem = new RubySimpleLookupItem(name, null, LookupValueWithPriority.NORMAL, true, null);
				variants.add(lookupItem);
			}
			// RUBY-806: adding __END__ to autocomplete variants
			final IElementType endType = RubyTokenTypes.tEND_MARKER;
			final String name = endType.toString();
			final RubySimpleLookupItem endLookupItem = new RubySimpleLookupItem(name, null, LookupValueWithPriority.NORMAL, false, null);
			variants.add(endLookupItem);
			RESWORDS_LOOKUP_ITEMS = variants;
		}
		return RESWORDS_LOOKUP_ITEMS;
	}

	@Nonnull
	private static List<RubyLookupItem> getScopeLocalVariables(@Nonnull final RPsiElement element)
	{
		final ArrayList<RubyLookupItem> variants = new ArrayList<RubyLookupItem>();
		final HashSet<String> names = new HashSet<String>();
		for(ScopeVariable scopeVariable : ScopeUtil.gatherScopeVariables(element))
		{
			final String newName = scopeVariable.getName();
			if(!names.contains(newName) && scopeVariable.getPrototype() != element)
			{
				names.add(newName);
				final Image icon = scopeVariable.isParameter() ? RubyIcons.RUBY_PARAMETER_NODE : RubyIcons.RUBY_VARIABLE_NODE;
				final String typeText = scopeVariable.isParameter() ? RBundle.message("parameter") : RBundle.message("local.variable");
				final RubyPsiLookupItem lookupItem = new RubyPsiLookupItem(element.getProject(), newName, null, typeText, scopeVariable.getPrototype(), LookupValueWithPriority.HIGH, true, icon);
				variants.add(lookupItem);
			}
		}
		return variants;
	}

}
