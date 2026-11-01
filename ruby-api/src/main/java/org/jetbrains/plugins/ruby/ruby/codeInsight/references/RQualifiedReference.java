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
import consulo.document.util.TextRange;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.ResolveCache;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubyLookupItem;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.ResolveUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Types;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.*;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.Message;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RTypeUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.impl.DuckTypeImpl;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.impl.MultiMessage;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.impl.RDuckTypeImpl;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.lang.findUsages.RubyUsageType;
import org.jetbrains.plugins.ruby.ruby.lang.findUsages.RubyUsageTypeProvider;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.names.RSuperClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RExpression;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.RPsiElementBase;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures.classes.RSuperClassNavigator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.expressions.RAssignmentExpressionNavigator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.references.RReference;
import org.jetbrains.plugins.ruby.ruby.presentation.SymbolPresentationUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: 08.05.2007
 */
public class RQualifiedReference implements RPsiPolyvariantReference
{
	protected RPsiElement myRefObject;
	protected PsiElement myRefValue;
	protected Project myProject;
	protected String myName;
	protected RPsiElement myWholeReference;
	protected RReference.Type myType;

	public RQualifiedReference(@Nonnull final Project project, @Nonnull final RPsiElement wholeReference, @Nullable final RPsiElement refObject, @Nonnull final PsiElement refValue, @Nonnull final RReference.Type type)
	{
		this(project, wholeReference, refObject, refValue, type, getName(wholeReference, refValue));
	}

	private static String getName(@Nonnull final RPsiElement wholeReference, @Nonnull final PsiElement refValue)
	{
		String name = refValue.getText();
		if(name.equals(RMethod.NEW))
		{
			return RMethod.INITIALIZE;
		}
		// Support for attr_writers
		if(RAssignmentExpressionNavigator.getAssignmentByLeftPart(wholeReference) != null)
		{
			if(TextUtil.isCID(name))
			{
				name += "=";
			}
		}
		return name;
	}

	public RQualifiedReference(@Nonnull final Project project, @Nonnull final RPsiElement wholeReference, @Nullable final RPsiElement refObject, @Nonnull final PsiElement refValue, @Nonnull final RReference.Type type, @Nonnull final String name)
	{
		myProject = project;
		myWholeReference = wholeReference;
		myRefObject = refObject;
		myRefValue = refValue;
		myType = type;
		myName = name;
	}

	private static class MyResolver implements ResolveCache.PolyVariantResolver<RQualifiedReference>
	{
		public static MyResolver INSTANCE = new MyResolver();

		@Override
		public ResolveResult[] resolve(RQualifiedReference ref, boolean incompleteCode)
		{
			return ref.multiResolveInner(incompleteCode);
		}
	}

	@Override
	@Nonnull
	public final ResolveResult[] multiResolve(final boolean incompleteCode)
	{
		final ResolveCache cache = ResolveCache.getInstance(myProject);
		return cache.resolveWithCaching(this, MyResolver.INSTANCE, false, false);
	}

	@Nonnull
	protected ResolveResult[] multiResolveInner(boolean incompleteCode)
	{
		if(myRefValue instanceof RPsiElementBase && ((RPsiElementBase) myRefValue).isClassOrModuleName())
		{
			return new ResolveResult[]{
					new ResolveResult()
					{
						@Override
						@Nullable
						public PsiElement getElement()
						{
							RubyUsageTypeProvider.setType(RQualifiedReference.this, RubyUsageType.DECLARATION);
							return myWholeReference.getParentContainer();
						}

						@Override
						public boolean isValidResult()
						{
							return true;
						}
					}
			};
		}

		final List<ResolveResult> list = new ArrayList<ResolveResult>();
		final FileSymbol fileSymbol = ((RPsiElementBase) myWholeReference).forceFileSymbolUpdate();

		final RType refObjectType = getRefObjectType(fileSymbol);
		final boolean refObjectTyped = refObjectType.isTyped();

		for(Symbol variant : multiResolveToSymbols(fileSymbol, refObjectType))
		{
			ResolveUtil.addVariants(fileSymbol, myProject, list, variant);
		}
		RubyUsageTypeProvider.setType(this, this instanceof Colon3Reference || refObjectTyped ? RubyUsageType.UNCLASSIFIED : RubyUsageType.EXPLICITLY_TYPED);

		return list.toArray(new ResolveResult[list.size()]);
	}


	@Override
	public PsiElement getElement()
	{
		return myWholeReference;
	}

	@Override
	@Nonnull
	public PsiElement getRefValue()
	{
		return myRefValue;
	}

	@Override
	public PsiElement resolve()
	{
		final ResolveResult[] resolveResults = multiResolve(true);
		return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
	}

	@Override
	public TextRange getRangeInElement()
	{
		final int relativeStartOffset = myRefValue.getTextOffset() - myWholeReference.getTextOffset();
		return new TextRange(relativeStartOffset, relativeStartOffset + myRefValue.getTextLength());
	}

	@Override
	public String getCanonicalText()
	{
		return myName;
	}

	@Override
	public PsiElement handleElementRename(String newName) throws IncorrectOperationException
	{
		// We shouldn`t rename if same name
		if(newName.equals(myName))
		{
			return null;
		}
		checkName(newName);
		final PsiElement element = RubyPsiUtil.getTopLevelElements(myProject, newName).get(0);
		RubyPsiUtil.replaceInParent(myRefValue, element);
		return element;
	}

	private static void checkName(@NonNls @Nonnull final String newName) throws IncorrectOperationException
	{
		if(!TextUtil.isCID(newName) && !TextUtil.isFID(newName))
		{
			throw new IncorrectOperationException(RBundle.message("rename.incorrect.name"));
		}
	}

	// called when a referenced foreign class is renamed or moved
	@Override
	public PsiElement bindToElement(@Nonnull PsiElement element) throws IncorrectOperationException
	{
		final RubyInteropProvider provider = RubyInterop.forElement(element);
		if(provider != null && provider.isClass(element) && element instanceof PsiNamedElement namedElement)
		{
			return handleElementRename(namedElement.getName());
		}
		return null;
	}

	@Override
	public boolean isReferenceTo(PsiElement element)
	{
		return ResolveUtil.isReferenceTo(this, element);
	}

	@Override
	public boolean isSoft()
	{
		return true;
	}

	@Override
	@Nonnull
	public List<Symbol> multiResolveToSymbols(@Nullable final FileSymbol fileSymbol)
	{
		return multiResolveToSymbols(fileSymbol, getRefObjectType(fileSymbol));
	}

	@Nonnull
	protected List<Symbol> multiResolveToSymbols(@Nullable final FileSymbol fileSymbol, @Nonnull final RType refObjectType)
	{
		if(((RPsiElementBase) myWholeReference).isClassOrModuleName())
		{
			return Collections.emptyList();
		}

		final ArrayList<Symbol> variants = new ArrayList<Symbol>();

		// add variants from refObjectType
		final boolean refObjectTyped = refObjectType.isTyped();

		final Collection<Message> messagesForName = refObjectType.getMessagesForName(myName);
		for(Message message : messagesForName)
		{
			if(message instanceof MultiMessage)
			{
				variants.addAll(((MultiMessage) message).getSymbols());
				continue;
			}
			final Symbol symbol = message.getSymbol();
			if(symbol != null)
			{
				variants.add(symbol);
			}
		}
		if(!refObjectTyped)
		{
			// add just all the symbols with given name
			variants.addAll(SymbolUtil.gatherAllSymbols(fileSymbol).
					getSymbolsByNameAndTypes(myName, Types.REFERENCE_AUTOCOMPLETE_TYPES).getAll());
		}
		return variants;
	}

	@Override
	public Object[] getVariants()
	{
		final FileSymbol fileSymbol = ((RPsiElementBase) myWholeReference).forceFileSymbolUpdate();
		if(fileSymbol == null)
		{
			return EMPTY_ARRAY;
		}

		myRefValue.putCopyableUserData(REFERENCE_BEING_COMPLETED, Boolean.TRUE);
		try
		{
			// RUBY-1363. Completion after "class Name <" should show only class names
			final RSuperClass superClass = RSuperClassNavigator.getByPsiElement(myWholeReference);
			final SymbolFilter filter = superClass != null ? SymbolFilterFactory.CLASSES_ONLY_FILTER : SymbolFilterFactory.EMPTY_FILTER;

			final List<RubyLookupItem> variants = new ArrayList<RubyLookupItem>();


			final RType refObjectType = getRefObjectType(fileSymbol);
			final boolean refObjectTyped = refObjectType.isTyped();

			variants.addAll(RTypeUtil.getLookupItemsByType(refObjectType, null, filter));
			if(!refObjectTyped)
			{
				// add just all the symbols with given name
				for(Symbol symbol : SymbolUtil.gatherAllSymbols(fileSymbol).getSymbolsOfTypes(Types.REFERENCE_AUTOCOMPLETE_TYPES).
						getChildrenByFilter(filter).getAll())
				{
					final String name = symbol.getName();
					if(name != null)
					{
						variants.add(SymbolPresentationUtil.createRubyLookupItem(symbol, name, false, false));
					}
				}
			}

			return variants.toArray(new Object[variants.size()]);
		}
		finally
		{
			myRefValue.putCopyableUserData(REFERENCE_BEING_COMPLETED, null);
		}

	}

	@Nonnull
	protected RType getRefObjectType(@Nullable final FileSymbol fileSymbol)
	{
		final RType refObjType = myRefObject instanceof RExpression ? ((RExpression) myRefObject).getType(fileSymbol) : RType.NOT_TYPED;
		final boolean isTyped = refObjType.isTyped();

		final DuckTypeImpl duckType = new DuckTypeImpl();
		for(Message message : refObjType.getMessages())
		{
			final Symbol symbol = message.getSymbol();
			if(symbol != null)
			{
				final Type type = symbol.getType();
				if(myType == RReference.Type.COLON_REF || myType == RReference.Type.DOT_REF && (Types.FOREIGN.contains(type) && type != Type.FOREIGN_FIELD || Types.METHODS_LIKE.contains(type)))
				{
					duckType.addMessage(message);
				}
			}
		}
		return new RDuckTypeImpl(duckType, isTyped);
	}

	// DO not modify!!!
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}

		RQualifiedReference reference = (RQualifiedReference) o;

		if(!myName.equals(reference.myName))
		{
			return false;
		}
		//noinspection RedundantIfStatement
		if(!myWholeReference.equals(reference.myWholeReference))
		{
			return false;
		}

		return true;
	}

	public int hashCode()
	{
		int result;
		result = myName.hashCode();
		result = 31 * result + myWholeReference.hashCode();
		return result;
	}
}
