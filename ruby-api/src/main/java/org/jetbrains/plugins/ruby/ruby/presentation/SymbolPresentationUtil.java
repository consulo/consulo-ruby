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

package org.jetbrains.plugins.ruby.ruby.presentation;

import org.jetbrains.plugins.ruby.ruby.lang.psi.RStructuralElement;
import org.jetbrains.plugins.ruby.ruby.cache.psi.StructureType;
import consulo.ruby.interop.RubyInterop;
import consulo.ruby.interop.RubyInteropProvider;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;

import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;

import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.fields.RField;

import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RConstant;

import consulo.component.util.Iconable;
import consulo.language.editor.completion.lookup.LookupValueWithPriority;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import consulo.ui.image.Image;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.RubyIcons;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.RAliasStatement;
import org.jetbrains.plugins.ruby.ruby.cache.psi.variables.RFieldAttr;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.ForeignLookupItem;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubyLookupItem;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubyPsiLookupItem;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubySimpleLookupItem;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.LastSymbolStorage;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Types;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.ForeignSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.lang.documentation.RubyHelpUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RVirtualPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures.methods.RCommandArgumentListImpl;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.global.RGlobalVariable;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Apr 20, 2007
 */
public class SymbolPresentationUtil
{

	@Nullable
	public static RubyLookupItem createRubyLookupItem(@Nonnull final Symbol symbol, @Nonnull String name, boolean bold, final boolean multiMessage)
	{
		final Type type = symbol.getType();
		if(type == Type.FILE)
		{
			return null;
		}

		// Local variable access
		if(type == Type.FIELD_WRITE_ACCESS)
		{
			return new RubySimpleLookupItem(name, RBundle.message("field.write"), LookupValueWithPriority.HIGH, true, RubyIcons.RUBY_FIELD_NODE);
		}
		if(type == Type.CONSTANT_ACCESS)
		{
			return new RubySimpleLookupItem(name, RBundle.message("constant"), LookupValueWithPriority.HIGH, true, RubyIcons.RUBY_NOT_DEFINED_NODE);
		}
		if(type == Type.CALL_ACCESS)
		{
			return new RubySimpleLookupItem(name, RBundle.message("call"), LookupValueWithPriority.HIGH, true, RubyIcons.RUBY_METHOD_NODE);
		}
		if(type == Type.ATTRIBUTE)
		{
			return new RubySimpleLookupItem(name, null, LookupValueWithPriority.HIGHER, true, RubyIcons.RUBY_ATTR_NODE);
		}

		if(Types.FOREIGN.contains(type))
		{
			return new ForeignLookupItem(name, ((ForeignSymbol) symbol).getPsiElement());
		}

		// Ruby symbols
		final Project project = symbol.getProject();
		final FileSymbol fileSymbol = LastSymbolStorage.getInstance(project).getSymbol();
		final RPsiElement lastPrototype = symbol.getLastVirtualPrototype(fileSymbol);

		String tailText = null;
		if(lastPrototype instanceof RMethod)
		{
			final RMethod method = (RMethod) lastPrototype;
			tailText = "(" + RCommandArgumentListImpl.getPresentableName(method.getArgumentInfos()) + ")";
		}

		int priority = bold ? LookupValueWithPriority.HIGH : LookupValueWithPriority.NORMAL;
		// Setting icon for lookupItem
		Image icon = null;
		if(lastPrototype instanceof RContainer)
		{
			// We should set high priority only for methods
			if(type != Type.INSTANCE_METHOD && type != Type.CLASS_METHOD && type != Type.ALIAS)
			{
				priority = LookupValueWithPriority.HIGHER;
			}
			icon = IconDescriptorUpdaters.getIcon(((PsiElement) lastPrototype), Iconable.ICON_FLAG_VISIBILITY);
		}
		else if(lastPrototype instanceof RField)
		{
			final RField field = (RField) lastPrototype;
			name = field.getText();
			icon = RFieldPresentationUtil.getIcon(field);
		}
		else if(lastPrototype instanceof RConstant)
		{
			icon = RConstantPresentationUtil.getIcon();
		}
		else if(lastPrototype instanceof RGlobalVariable)
		{
			priority = LookupValueWithPriority.NORMAL;
			bold = true;
			icon = RGlobalVariablePresentationUtil.getIcon();
		}
		else if(lastPrototype instanceof RAliasStatement)
		{
			icon = RAliasPresentationUtil.getIcon();
		}
		else if(isForeignImport(lastPrototype))
		{
			bold = true;
			icon = ForeignImportPresentationUtil.getIncludeIcon();
		}
		else if(lastPrototype instanceof RFieldAttr)
		{
			icon = RFieldAttrPresentationUtil.getAttrIcon(((RFieldAttr) lastPrototype).getFieldAttrType());
		}
		else if(type == Type.NOT_DEFINED)
		{
			icon = RContainerPresentationUtil.RUBY_NOT_DEFINED_NODE;
		}

		final Symbol parent = symbol.getParentSymbol();
		assert parent != null;
		String typeText = parent.getType() != Type.FILE ? SymbolUtil.getPresentablePath(parent) : "";
		// See RUBY-1302. Show help for global variables
		if(lastPrototype instanceof RGlobalVariable)
		{
			final RPsiElement elem = RVirtualPsiUtil.findPsiByVirtualElement(lastPrototype, symbol.getProject());
			if(elem instanceof RGlobalVariable)
			{
				typeText = RubyHelpUtil.getPsiHelp(elem);
				if(typeText != null)
				{
					typeText = typeText.substring(1);
					int i = typeText.indexOf('.');
					if(i != -1)
					{
						typeText = typeText.substring(0, i);
					}
					i = typeText.indexOf('\n');
					if(i != -1)
					{
						typeText = typeText.substring(0, i);
					}
					typeText = typeText.trim();
				}
			}
		}
		if(multiMessage)
		{
			typeText = "...";
		}
		return lastPrototype != null ? new RubyPsiLookupItem(project, name, tailText, typeText, lastPrototype, priority, bold, icon) : new RubySimpleLookupItem(name, typeText, priority, bold, icon);
	}

	@Nullable
	/**
	 * Creates presentable name for symbol with location
	 */
	public static String getPresentableNameWithLocation(@Nullable final FileSymbol fileSymbol, @Nonnull final Symbol symbol)
	{
		String name = symbol.getName();
		if(name == null)
		{
			return null;
		}
		if(Types.FOREIGN.contains(symbol.getType()))
		{
			final PsiElement element = ((ForeignSymbol) symbol).getPsiElement();
			final RubyInteropProvider provider = RubyInterop.forElement(element);
			if(provider == null)
			{
				return null;
			}
			final String containingClassName = provider.getContainingClassName(element);
			return containingClassName != null ? name + " " + RBundle.message("in") + " " + containingClassName : provider.getQualifiedName(element);
		}
		final RPsiElement lastPrototype = symbol.getLastVirtualPrototype(fileSymbol);
		if(lastPrototype instanceof RMethod)
		{
			final RMethod method = (RMethod) lastPrototype;
			name += "(" + RCommandArgumentListImpl.getPresentableName(method.getArgumentInfos()) + ")";
		}

		final Symbol parent = symbol.getParentSymbol();
		assert parent != null;
		return parent.getType() != Type.FILE ? name + " " + RBundle.message("in") + " " + SymbolUtil.getPresentablePath(parent) : name;
	}


	@Nonnull
	public static List<RPsiElement> getPrototypesToShow(@Nullable final FileSymbol fileSymbol, @Nonnull final Symbol symbol)
	{
		final List<RPsiElement> list = new ArrayList<RPsiElement>();
		// We show only last prototype for method!!!
		if(Types.METHODS.contains(symbol.getType()))
		{
			final RPsiElement element = symbol.getLastVirtualPrototype(fileSymbol);
			if(element != null)
			{
				list.add(element);
			}
		}
		else
		{
			list.addAll(symbol.getVirtualPrototypes(fileSymbol).getAll());
		}
		return list;
	}


	private static boolean isForeignImport(@Nullable final RPsiElement element)
	{
		if(!(element instanceof RStructuralElement structuralElement))
		{
			return false;
		}
		final StructureType type = structuralElement.getType();
		return type == StructureType.CALL_IMPORT || type == StructureType.CALL_INCLUDE_CLASS || type == StructureType.CALL_INCLUDE_PACKAGE;
	}
}
