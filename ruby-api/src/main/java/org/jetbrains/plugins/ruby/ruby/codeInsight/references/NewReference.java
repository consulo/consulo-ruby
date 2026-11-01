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

package org.jetbrains.plugins.ruby.ruby.codeInsight.references;

import consulo.language.psi.PsiNamedElement;
import consulo.ruby.interop.RubyInterop;
import consulo.ruby.interop.RubyInteropProvider;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.ResolveUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.ForeignSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.RPsiElementBase;
import org.jetbrains.plugins.ruby.ruby.lang.psi.references.RReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Jan 16, 2008
 */
public class NewReference extends RQualifiedReference
{
	public NewReference(@Nonnull final Project project, @Nonnull final RPsiElement wholeReference, @Nullable final RPsiElement refObject, @Nonnull final PsiElement refValue)
	{
		super(project, wholeReference, refObject, refValue, RReference.Type.COLON_REF, RMethod.INITIALIZE);
	}

	@Override
	@Nonnull
	public List<Symbol> multiResolveToSymbols(@Nullable final FileSymbol fileSymbol)
	{
		if(((RPsiElementBase) myWholeReference).isClassOrModuleName())
		{
			return Collections.emptyList();
		}

		// We process only FOREIGN_CLASS here
		final List<Symbol> symbols = ResolveUtil.resolveToSymbols(myRefObject);
		if(symbols.size() == 1)
		{
			final Symbol symbol = symbols.get(0);
			final PsiElement element = symbol.getType() == Type.FOREIGN_CLASS || symbol.getType() == Type.FOREIGN_PROXY_CLASS ? RubyInterop.getElement(symbol) : null;
			final RubyInteropProvider provider = RubyInterop.forElement(element);
			if(provider != null && provider.isClass(element))
			{
				final ArrayList<Symbol> variants = new ArrayList<Symbol>();
				for(PsiElement constructor : provider.getConstructors(element))
				{
					if(constructor instanceof PsiNamedElement namedConstructor && namedConstructor.getName() != null)
					{
						variants.add(new ForeignSymbol(constructor, namedConstructor.getName(), null, Type.FOREIGN_METHOD));
					}
				}
				return variants;
			}
		}
		return super.multiResolveToSymbols(fileSymbol);
	}
}
