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

package org.jetbrains.plugins.ruby.ruby.codeInsight.resolve;

import org.jetbrains.plugins.ruby.ruby.lang.psi.RStructuralElement;
import org.jetbrains.plugins.ruby.ruby.cache.psi.StructureType;
import consulo.ruby.interop.RubyInterop;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import consulo.project.Project;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RImportJavaClass;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RIncludeJavaClass;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RIncludeJavaPackage;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RVirtualName;
import org.jetbrains.plugins.ruby.ruby.codeInsight.references.RPsiPolyvariantReference;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Types;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.ForeignSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.PsiElementSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.lang.findUsages.RubyUsageType;
import org.jetbrains.plugins.ruby.ruby.lang.findUsages.RubyUsageTypeProvider;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RVirtualPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.RPsiElementBase;
import org.jetbrains.plugins.ruby.ruby.presentation.SymbolPresentationUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiPolyVariantReference;
import consulo.language.psi.PsiReference;
import consulo.language.psi.ResolveResult;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Apr 20, 2007
 */
public class ResolveUtil
{
	/**
	 * Adds set of virtualPrototypes to resolve list
	 *
	 * @param fileSymbol FileSymbol
	 * @param project    Current project
	 * @param list       resolve list to add to
	 * @param symbol     Symbol to add to Resolveresults
	 */
	public static void addVariants(@Nullable final FileSymbol fileSymbol, @Nonnull final Project project, @Nonnull final List<ResolveResult> list, @Nonnull final Symbol symbol)
	{

		// Foreign symbol
		if(Types.FOREIGN.contains(symbol.getType()))
		{
			add(list, ((ForeignSymbol) symbol).getPsiElement());
		}
		// Local variable
		if(Type.LOCAL_VARIABLE.asSet().contains(symbol.getType()))
		{
			add(list, ((PsiElementSymbol) symbol).getPsiElement());
		}

		for(RPsiElement element : SymbolPresentationUtil.getPrototypesToShow(fileSymbol, symbol))
		{
			final StructureType elementType = element instanceof RStructuralElement structuralElement ? structuralElement.getType() : null;
			if(elementType == StructureType.CALL_IMPORT && element instanceof RImportJavaClass)
			{
				for(RVirtualName name : ((RImportJavaClass) element).getVirtualNames())
				{
					add(list, RubyInterop.compute(provider -> provider.findPackageOrClass(project, name.getPath())));
				}
			}
			if((elementType == StructureType.CALL_INCLUDE_CLASS || elementType == StructureType.CALL_INCLUDE_PACKAGE) && element instanceof RIncludeJavaClass)
			{
				String qualifiedName = ((RIncludeJavaClass) element).getQualifiedName();
				if(qualifiedName != null)
				{
					// include_package defines every class of the package
					if(elementType == StructureType.CALL_INCLUDE_PACKAGE)
					{
						qualifiedName += '.' + symbol.getName();
					}
					final String name = qualifiedName;
					add(list, RubyInterop.compute(provider -> provider.findPackageOrClass(project, name)));
				}
			}
			add(list, RVirtualPsiUtil.findPsiByVirtualElement(element, project));
		}
	}

	private static void add(@Nonnull final List<ResolveResult> list, final PsiElement psiElement)
	{
		if(psiElement != null)
		{
			list.add(new ResolveResult()
			{
				@Override
				@Nullable
				public PsiElement getElement()
				{
					return psiElement;
				}

				@Override
				public boolean isValidResult()
				{
					return true;
				}
			});
		}
	}

	/**
	 * Resolves element to the set of symbols
	 *
	 * @param element Element to resolve
	 * @return The list of symbols
	 */
	public static List<Symbol> resolveToSymbols(@Nullable final PsiElement element)
	{
		if(element == null)
		{
			return Collections.emptyList();
		}
		final PsiReference ref = element.getReference();
		if(ref instanceof RPsiPolyvariantReference)
		{
			FileSymbol fileSymbol = null;
			if(element instanceof RFile)
			{
				fileSymbol = ((RFile) element).getFileSymbol();
			}
			else if(element instanceof RPsiElementBase)
			{
				fileSymbol = ((RPsiElementBase) element).forceFileSymbolUpdate();
			}
			return ((RPsiPolyvariantReference) ref).multiResolveToSymbols(fileSymbol);
		}
		return Collections.emptyList();
	}


	@Nullable
	public static PsiElement resolvePolyVarReference(@Nonnull final RPsiPolyvariantReference ref)
	{
		final ResolveResult[] results = ref.multiResolve(false);
		return results.length == 1 ? results[0].getElement() : null;
	}

	@Nullable
	public static Symbol resolveToSymbol(@Nullable final FileSymbol fileSymbol, @Nullable final PsiReference ref)
	{
		if(ref instanceof RPsiPolyvariantReference)
		{
			final List<Symbol> symbols = ((RPsiPolyvariantReference) ref).multiResolveToSymbols(fileSymbol);
			return symbols.size() == 1 ? symbols.get(0) : null;
		}
		return null;
	}

	public static boolean isReferenceTo(@Nonnull final PsiReference reference, final PsiElement element)
	{
		if(reference instanceof PsiPolyVariantReference)
		{
			for(ResolveResult result : ((PsiPolyVariantReference) reference).multiResolve(true))
			{
				if(result.getElement() == element)
				{
					return true;
				}
			}
		}
		else if(reference.resolve() == element)
		{
			return true;
		}
		RubyUsageTypeProvider.setType(reference, RubyUsageType.TEXT_MATCHED);
		return false;
	}

	@Nonnull
	public static List<PsiElement> multiResolve(@Nonnull final PsiElement element)
	{
		final PsiReference ref = element.getReference();
		if(ref instanceof PsiPolyVariantReference)
		{
			final ResolveResult[] results = ((PsiPolyVariantReference) ref).multiResolve(false);
			final ArrayList<PsiElement> result = new ArrayList<PsiElement>(results.length);
			for(ResolveResult resolveResult : results)
			{
				result.add(resolveResult.getElement());
			}
			return result;
		}

		final PsiElement res = ref != null ? ref.resolve() : null;
		return res != null ? Collections.singletonList(res) : Collections.<PsiElement>emptyList();
	}
}
