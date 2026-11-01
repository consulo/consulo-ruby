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

import consulo.language.psi.util.PsiTreeUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RAssocList;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.assoc.RAssoc;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RListOfExpressions;
import consulo.language.psi.PsiElement;

/**
 * @author yole
 */
public class ResolvingParamDependency implements ParamDependency<PsiElement>
{
	private final String myKey;

	public ResolvingParamDependency(final String key)
	{
		myKey = key;
	}

	@Override
	@Nullable
	public PsiElement getValue(final ParamContext context)
	{
		RListOfExpressions args = context.getCall().getCallArguments();
		List<RAssoc> assocs = new ArrayList<RAssoc>();
		for(RPsiElement element : args.getElements())
		{
			if(element instanceof RAssoc assoc)
			{
				assocs.add(assoc);
			}
			else if(element instanceof RAssocList)
			{
				assocs.addAll(PsiTreeUtil.getChildrenOfTypeAsList(element, RAssoc.class));
			}
		}
		for(RAssoc rAssoc : assocs)
		{
			{
				RPsiElement value = rAssoc.getValue();
				if(value != null)
				{
					if(myKey.equals(rAssoc.getKeyName()))
					{
						ParamContext dependencyContext = new ParamContext(value, context.getCall(), context.getIndex(), myKey);
						ParamDef dependencyParamDef = ParamDefUtil.getParamDef(dependencyContext);
						if(dependencyParamDef != null)
						{
							PsiElement result = dependencyParamDef.resolveReference(dependencyContext);
							if(result != null)
							{
								return result;
							}
						}
					}
				}
			}
		}
		return null;
	}
}
