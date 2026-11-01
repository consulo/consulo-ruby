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

package org.jetbrains.plugins.ruby.ruby.codeInsight.paramInfo;

import consulo.ruby.interop.RubyInterop;
import consulo.ruby.interop.RubyInteropProvider;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;

import org.jetbrains.plugins.ruby.ruby.lang.psi.RStructuralElement;

import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.editor.CodeInsightBundle;
import consulo.language.editor.CodeInsightSettings;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.parameterInfo.*;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiWhiteSpace;
import consulo.language.psi.util.PsiTreeUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.ResolveUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.LastSymbolStorage;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Types;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.ForeignSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.lang.RubyLanguage;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.psi.*;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.blocks.RCompoundStatement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.ArgumentInfo;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RArgumentList;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RListOfExpressions;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Jul 2, 2007
 */
@ExtensionImpl
public class RubyParameterInfoHandler implements ParameterInfoHandler<RPossibleCall, Symbol>
{
	public static final String DEFAULT_PARAMETER_CLOSE_CHARS = "{},);\n";

	@Override
	public boolean couldShowInLookup()
	{
		return true;
	}

	@Override
	@Nullable
	public Object[] getParametersForLookup(LookupElement item, ParameterInfoContext context)
	{
		return null;
	}

	@Override
	@Nullable
	public Object[] getParametersForDocumentation(Symbol symbol, ParameterInfoContext context)
	{
		return null;
	}

	@Override
	public RPossibleCall findElementForParameterInfo(@Nonnull final CreateParameterInfoContext context)
	{
		return findCall(context);
	}

	private RPossibleCall findCall(@Nonnull final CreateParameterInfoContext context)
	{
		final PsiFile file = context.getFile();
		final int offset = context.getOffset();
		final RPossibleCall call = findCall(file, offset);
		if(call != null)
		{
			final ArrayList<Symbol> methods = SymbolUtil.filterMethods(tryToResolveCommand(call), Types.METHODS);
			if(methods.size() == 0)
			{
				return null;
			}
			context.setItemsToShow(methods.toArray());
			return call;
		}
		return null;
	}

	@Override
	public void showParameterInfo(@Nonnull final RPossibleCall element, @Nonnull final CreateParameterInfoContext context)
	{
		context.showHint(element, element.getTextRange().getStartOffset(), this);
	}

	@Override
	public RPossibleCall findElementForUpdatingParameterInfo(@Nonnull final UpdateParameterInfoContext context)
	{
		return findCall(context.getFile(), context.getOffset());
	}

	@Override
	public String getParameterCloseChars()
	{
		return DEFAULT_PARAMETER_CLOSE_CHARS;
	}

	@Override
	public boolean tracksParameterIndex()
	{
		return true;
	}

	@Override
	public void updateUI(@Nonnull final Symbol symbol, @Nonnull final ParameterInfoUIContext context)
	{
		if(symbol.getType() == Type.FOREIGN_METHOD)
		{
			final PsiElement element = ((ForeignSymbol) symbol).getPsiElement();
			final RubyInteropProvider provider = RubyInterop.forElement(element);
			if(provider != null)
			{
				provider.updateParameterInfo(element, context);
			}
		}
		else
		{
			final RPsiElement prototype = symbol.getLastVirtualPrototype(LastSymbolStorage.getInstance(symbol.getProject()).getSymbol());
			if(prototype instanceof RMethod)
			{
				final RStructuralElement element = RVirtualPsiUtil.findInPsi(symbol.getProject(), (RMethod) prototype);
				if(element instanceof RMethod)
				{
					updateRMethodPresentation((RMethod) element, context);
				}
			}
		}
	}


	public static void updateRMethodPresentation(@Nonnull final RMethod method, @Nonnull final ParameterInfoUIContext context)
	{
		// Index to show
		final int index = context.getCurrentParameterIndex();

		final StringBuilder buff = new StringBuilder();
		// here we store index of current argument
		int start = -1;
		int end = -1;

		final RArgumentList argumentList = method.getArgumentList();
		if(argumentList != null)
		{
			final List<ArgumentInfo> argList = argumentList.getArgumentInfos(false);
			if(!argList.isEmpty())
			{
				for(int i = 0; i < argList.size(); i++)
				{
					if(i > 0)
					{
						buff.append(", ");
					}
					final ArgumentInfo argumentInfo = argList.get(i);
					final String argumentText = argumentInfo.getPresentableName();

					if(start == -1 && (i == index || argumentInfo.getType() == ArgumentInfo.Type.ARRAY && index != -1))
					{
						start = buff.length();
						end = start + argumentText.length();
					}
					buff.append(argumentText);
				}

				if(CodeInsightSettings.getInstance().SHOW_FULL_SIGNATURES_IN_PARAMETER_INFO)
				{
					// Here we add method name
					final String prefix = method.getName() + "(";
					final String postfix = ")";
					buff.insert(0, prefix);
					// If we have found parameter we should move it to the length of prefix
					if(start != -1)
					{
						start += prefix.length();
						end += prefix.length();
					}
					buff.append(postfix);
				}
			}
			else
			{
				buff.append(CodeInsightBundle.message("parameter.info.no.parameters"));
			}
		}
		else
		{
			buff.append(CodeInsightBundle.message("parameter.info.no.parameters"));
		}

		context.setupUIComponentPresentation(buff.toString(), start, end, !context.isUIComponentEnabled(), false, false, context.getDefaultParameterColor());

	}

	/*
	* Finds rCall or just identifier call
	*/
	@Nullable
	public static RPossibleCall findCall(@Nullable final PsiFile file, int offset)
	{
		if(!(file instanceof RFile))
		{
			return null;
		}

		final CharSequence chars = file.getViewProvider().getContents();
		if(offset >= chars.length())
		{
			offset = chars.length() - 1;
		}
		PsiElement element = file.findElementAt(offset);
		if(element == null)
		{
			return null;
		}
		final ASTNode node = element.getNode();
		if(node != null && node.getElementType() == RubyTokenTypes.tEOL)
		{
			element = element.getPrevSibling();
		}
		while(element instanceof PsiWhiteSpace)
		{
			element = element.getPrevSibling();
		}

		// If we`re staying in the end of file, just after some identifier, our previous element is RCompoundStatement
		if(element instanceof RCompoundStatement)
		{
			final List<RPsiElement> list = ((RCompoundStatement) element).getStatements();
			final int size = list.size();
			element = size > 0 ? list.get(size - 1) : null;
		}

		// Try to find RPossibleCall
		RPossibleCall call = element instanceof RPossibleCall ? (RPossibleCall) element : PsiTreeUtil.getParentOfType(element, RPossibleCall.class);
		// If we`re staying on local variable we should take parent
		if(call instanceof RIdentifier)
		{
			final RIdentifier id = (RIdentifier) call;
			if(id.isParameter() || id.isLocalVariable())
			{
				call = PsiTreeUtil.getParentOfType(call, RPossibleCall.class);
			}
		}
		while(call != null && tryToResolveCommand(call).isEmpty())
		{
			call = PsiTreeUtil.getParentOfType(call, RPossibleCall.class);
		}
		return call;
	}

	@Nonnull
	private static List<Symbol> tryToResolveCommand(@Nonnull final RPossibleCall element)
	{
		if(element instanceof RCall)
		{
			return ResolveUtil.resolveToSymbols(((RCall) element).getPsiCommand());
		}
		else
		{
			return ResolveUtil.resolveToSymbols(element);
		}
	}

	@Override
	public void updateParameterInfo(@Nonnull final RPossibleCall element, @Nonnull final UpdateParameterInfoContext context)
	{
		int index = -1;
		final int carret = context.getOffset();
		if(element instanceof RCall)
		{
			final RListOfExpressions callArgs = ((RCall) element).getCallArguments();
			index = ParameterInfoUtils.getCurrentParameterIndex(callArgs.getNode(), carret, RubyTokenTypes.tCOMMA);
			// If we are just before the arguments
			if(index == -1 && callArgs.getTextOffset() == carret + 1)
			{
				index = 0;
			}
		}
		else
		{
			if(carret > element.getTextRange().getEndOffset())
			{
				index = 0;
			}
		}
		context.setCurrentParameter(index);
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return RubyLanguage.INSTANCE;
	}
}
