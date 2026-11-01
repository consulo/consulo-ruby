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

package org.jetbrains.plugins.ruby.ruby.codeInsight.symbols;

import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;

import consulo.language.editor.generation.ClassMember;
import consulo.language.psi.PsiElement;
import consulo.ruby.interop.RubyInterop;
import consulo.ruby.interop.RubyInteropProvider;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.ForeignSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.data.Children;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.Context;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.CoreTypes;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Jan 10, 2008
 */
public class RubyOverrideImplementUtil
{
	/*
	 * Gathers override virtual elements for given virtual element
	 * returns list of RPsiElement or foreign methods
	 */
	@Nonnull
	public static List getOverridenElements(@Nullable final FileSymbol fileSymbol, @Nonnull final Symbol symbol, @Nullable final RPsiElement anchorElement)
	{
		return getOverridenElements(fileSymbol, symbol, anchorElement, getOverridenSymbols(fileSymbol, symbol));
	}

	/*
	 * Gathers override virtual elements for given virtual element
	 * returns list of RPsiElement or foreign methods
	 */
	@SuppressWarnings({"unchecked"})
	@Nonnull
	public static List getOverridenElements(@Nullable final FileSymbol fileSymbol, @Nonnull final Symbol symbol, @Nullable final RPsiElement anchorElement, @Nonnull final List<Symbol> overridenSymbols)
	{
		final ArrayList elements = new ArrayList();
		// we should add all the prototypes of overriden symbols
		for(Symbol s : overridenSymbols)
		{
			if(s.getType() == Type.FOREIGN_METHOD)
			{
				final PsiElement method = ((ForeignSymbol) s).getPsiElement();
				final RubyInteropProvider provider = RubyInterop.forElement(method);
				if(method.isValid() && provider != null && provider.isOverridableMethod(method))
				{
					elements.add(method);
				}
			}
			elements.addAll(s.getVirtualPrototypes(fileSymbol).getAll());
		}

		// and all the prototypes of given symbol before anchorElement
		for(RPsiElement element : symbol.getVirtualPrototypes(fileSymbol).getAll())
		{
			if(element == anchorElement)
			{
				break;
			}
			elements.add(element);
		}
		return elements;
	}

	/**
	 * Gathers the abstract foreign methods the overridden symbols implement
	 */
	@Nonnull
	public static List<PsiElement> getImplementedForeignMethods(@Nonnull final List<Symbol> overridenSymbols)
	{
		final List<PsiElement> implemented = new ArrayList<PsiElement>();
		for(Symbol s : overridenSymbols)
		{
			if(s.getType() == Type.FOREIGN_METHOD)
			{
				final PsiElement method = ((ForeignSymbol) s).getPsiElement();
				final RubyInteropProvider provider = RubyInterop.forElement(method);
				if(method.isValid() && provider != null && provider.isAbstractMethod(method))
				{
					implemented.add(method);
				}
			}
		}
		return implemented;
	}

	/*
	 * Gathers override symbols for given symbol, e.g. overriden methods for methods
	 */
	@Nonnull
	public static List<Symbol> getOverridenSymbols(@Nullable final FileSymbol fileSymbol, @Nonnull final Symbol symbol)
	{
		final Type type = symbol.getType();
		final String name = symbol.getName();
		if(name == null)
		{
			return Collections.emptyList();
		}
		final List<Symbol> overridenSymbols = new ArrayList<Symbol>();
		// For methods we look for overriden methods
		if(Types.METHODS_LIKE.contains(type))
		{
			final Symbol parent = symbol.getParentSymbol();
			if(parent != null && Types.MODULE_OR_CLASS.contains(parent.getType()))
			{
				final Children children;
				if(Types.INSTANCE_TYPES.contains(type))
				{
					children = SymbolUtil.getAllChildrenWithSuperClassesAndIncludes(fileSymbol, Context.INSTANCE, parent, symbol);
				}
				else if(Types.STATIC_TYPES.contains(type))
				{
					children = SymbolUtil.getAllChildrenWithSuperClassesAndIncludes(fileSymbol, Context.CLASS, parent, symbol);
				}
				else
				{
					children = SymbolUtil.getAllChildrenWithSuperClassesAndIncludes(fileSymbol, Context.ALL, parent, symbol);
				}
				for(Symbol method : children.getSymbolsByNameAndTypes(name, Types.METHODS_LIKE).getAll())
				{
					// We add this hack not to tell, that method is overriden by itself
					if(symbol != method)
					{
						overridenSymbols.add(method);
					}
				}
			}
		}
		return overridenSymbols;
	}

	/**
	 * Gathers the methods of the superclasses and included modules the class may override: not defined by the class yet,
	 * no constructors, foreign methods only when they are overridable
	 */
	@Nonnull
	public static List<Symbol> getMethodsToOverride(@Nullable final FileSymbol fileSymbol, @Nonnull final Symbol classSymbol)
	{
		final Set<String> seenNames = new HashSet<String>();
		for(Symbol own : classSymbol.getChildren(fileSymbol).getSymbolsOfTypes(Types.METHODS).getAll())
		{
			seenNames.add(own.getName());
		}

		final List<Symbol> methods = new ArrayList<Symbol>(SymbolUtil.getAllChildrenWithSuperClassesAndIncludes(fileSymbol, Context.INSTANCE, classSymbol, null).getSymbolsOfTypes(Types.METHODS).getAll());
		Collections.reverse(methods);

		final List<Symbol> result = new ArrayList<Symbol>();
		for(Symbol method : methods)
		{
			final String name = method.getName();
			if(name == null || seenNames.contains(name) || method.getParentSymbol() == classSymbol || RMethod.NEW.equals(name) || RMethod.INITIALIZE.equals(name) || isMetaClassMethod(method))
			{
				continue;
			}
			if(method.getType() == Type.FOREIGN_METHOD)
			{
				final PsiElement element = ((ForeignSymbol) method).getPsiElement();
				final RubyInteropProvider provider = RubyInterop.forElement(element);
				if(provider == null || !element.isValid() || !provider.isOverridableMethod(element))
				{
					continue;
				}
			}
			seenNames.add(name);
			result.add(method);
		}
		return result;
	}

	/**
	 * Gathers the abstract foreign methods (e.g. of an included Java interface) the class does not define
	 */
	@Nonnull
	public static List<Symbol> getMethodsToImplement(@Nullable final FileSymbol fileSymbol, @Nonnull final Symbol classSymbol)
	{
		final Children children = SymbolUtil.getAllChildrenWithSuperClassesAndIncludes(fileSymbol, Context.INSTANCE, classSymbol, null);

		final Set<String> seenNames = new HashSet<String>();
		for(Symbol method : children.getSymbolsOfTypes(Types.METHODS).getAll())
		{
			if(method.getType() != Type.FOREIGN_METHOD || !isAbstractForeignMethod(method))
			{
				seenNames.add(method.getName());
			}
		}

		final List<Symbol> foreignMethods = new ArrayList<Symbol>(children.getSymbolsOfTypes(Type.FOREIGN_METHOD.asSet()).getAll());
		Collections.reverse(foreignMethods);

		final List<Symbol> result = new ArrayList<Symbol>();
		for(Symbol method : foreignMethods)
		{
			final String name = method.getName();
			if(name != null && !seenNames.contains(name) && isAbstractForeignMethod(method))
			{
				seenNames.add(name);
				result.add(method);
			}
		}
		return result;
	}

	/**
	 * The instance methods of Class and Module come with the class object, its instances do not have them
	 */
	private static boolean isMetaClassMethod(@Nonnull final Symbol method)
	{
		final Symbol parent = method.getParentSymbol();
		return parent != null && parent.getParentSymbol() != null && parent.getParentSymbol().getType() == Type.FILE
				&& (CoreTypes.Class.equals(parent.getName()) || CoreTypes.Module.equals(parent.getName()));
	}

	private static boolean isAbstractForeignMethod(@Nonnull final Symbol method)
	{
		final PsiElement element = ((ForeignSymbol) method).getPsiElement();
		final RubyInteropProvider provider = RubyInterop.forElement(element);
		return provider != null && element.isValid() && provider.isAbstractMethod(element);
	}

	public static String classMembersToString(@Nonnull final List<ClassMember> list)
	{
		final StringBuilder builder = new StringBuilder();
		for(ClassMember member : list)
		{
			builder.append(member.getText()).append("\n");
		}
		return builder.toString().trim();
	}

}
