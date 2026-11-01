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

package org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs;

import java.util.List;

import consulo.language.psi.PsiElement;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.ResolveUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SpecialSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.assoc.RAssoc;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RArray;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RAssocList;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RListOfExpressions;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import consulo.application.ApplicationManager;
import org.jetbrains.plugins.ruby.rails.facet.RailsFacetUtil;

/**
 * @author yole
 */
public class ParamDefUtil
{
	public static ParamContext getParamContext(RPsiElement element)
	{
		return getParamContext(element, element);
	}

	private static ParamContext getParamContext(RPsiElement element, final RPsiElement valueElement)
	{
		RListOfExpressions exprList = null;
		int exprIndex = -1;
		PsiElement parent = element.getParent();
		RAssoc assoc = null;
		if(parent instanceof RListOfExpressions)
		{
			exprList = (RListOfExpressions) parent;
			List<RPsiElement> expressions = exprList.getElements();
			exprIndex = expressions.indexOf(element);
		}
		else
		{
			PsiElement parent2 = parent.getParent();
			if(parent2 instanceof RListOfExpressions)
			{
				if(parent instanceof RAssoc)
				{
					assoc = (RAssoc) parent;
				}
				exprList = (RListOfExpressions) parent2;
				List<RPsiElement> expressions = exprList.getElements();
				exprIndex = expressions.indexOf(parent);
			}
			else if(parent2 instanceof RAssocList && parent2.getParent() instanceof RListOfExpressions)
			{
				if(parent instanceof RAssoc)
				{
					assoc = (RAssoc) parent;
				}
				exprList = (RListOfExpressions) parent2.getParent();
				List<RPsiElement> expressions = exprList.getElements();
				exprIndex = expressions.indexOf(parent2);
			}
		}
		if(exprList != null)
		{
			if(exprList.getParent() instanceof RArray)
			{
				return getParamContext((RArray) exprList.getParent(), valueElement);
			}
			if(exprList.getParent() instanceof RCall)
			{
				RCall call = (RCall) exprList.getParent();
				String hashKey = null;
				if(assoc != null)
				{
					hashKey = assoc.getKeyName();
				}
				return new ParamContext(valueElement, call, exprIndex, hashKey);
			}
		}
		return null;
	}

	@Nullable
	public static ParamDef getParamDef(final ParamContext context)
	{
		final List<Symbol> symbols = ResolveUtil.resolveToSymbols(context.getCall().getPsiCommand());
		if(symbols.size() > 0)
		{
			Symbol symbol = symbols.get(0);
			if(symbol instanceof SpecialSymbol)
			{
				final SpecialSymbol specialSymbol = (SpecialSymbol) symbol;
				if(specialSymbol.getType() == Type.ALIAS)
				{
					symbol = specialSymbol.getLinkedSymbol();
				}
			}
			return getParamDefForSymbol(context, symbol);
		}
		else if(ApplicationManager.getApplication().isUnitTestMode())
		{
			String calledMethod = context.getCall().getPsiCommand().getText();
			ParamDef[] paramDefs = ParamDefManager.getInstance().getParamDefs(calledMethod);
			return findParamDefByIndex(context, paramDefs);
		}
		else if(context.getModule() != null && RailsFacetUtil.hasRailsSupport(context.getModule()))
		{
			// the Rails sources are not always resolved: match the framework methods by name
			String calledMethod = context.getCall().getPsiCommand().getText();
			return findParamDefByIndex(context, ParamDefManager.getInstance().getParamDefsByMethodName(calledMethod));
		}
		return null;
	}

	public static ParamDef getParamDefForSymbol(final ParamContext context, final Symbol symbol)
	{
		final ParamDef[] paramDefs = ParamDefManager.getInstance().getParamDefs(symbol);
		return findParamDefByIndex(context, paramDefs);
	}

	public static ParamDef findParamDefByIndex(final ParamContext context, final ParamDef[] paramDefs)
	{
		ParamDef paramDef = null;
		if(paramDefs != null)
		{
			if(paramDefs.length > context.getIndex())
			{
				paramDef = paramDefs[context.getIndex()];
			}
			else if(paramDefs.length > 0)
			{
				ParamDef lastParamDef = paramDefs[paramDefs.length - 1];
				if(lastParamDef instanceof HashParamDef || lastParamDef instanceof ListParamDef)
				{
					paramDef = lastParamDef;
				}
			}
		}
		if(context.getHashKey() != null && paramDef instanceof HashParamDef)
		{
			return ((HashParamDef) paramDef).getValueParamDef(context.getHashKey());
		}
		return paramDef;
	}
}
