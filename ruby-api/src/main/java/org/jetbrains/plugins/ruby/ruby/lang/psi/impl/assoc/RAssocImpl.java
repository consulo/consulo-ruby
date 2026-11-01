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

package org.jetbrains.plugins.ruby.ruby.lang.psi.impl.assoc;

import consulo.language.psi.PsiElement;
import consulo.util.lang.StringUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamDefImplUtil;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.psi.basicTypes.RSymbol;
import org.jetbrains.plugins.ruby.ruby.lang.psi.basicTypes.stringLiterals.RStringLiteral;
import jakarta.annotation.Nullable;

import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.StubElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.assoc.RAssoc;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.RPsiElementBase;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 08.06.2006
 */
public class RAssocImpl extends RPsiElementBase<StubElement> implements RAssoc
{
	public RAssocImpl(ASTNode astNode)
	{
		super(astNode);
	}

	@Override
	@Nullable
	public RPsiElement getKey()
	{
		return getLabel() != null ? null : getChildByType(RPsiElement.class, 0);
	}

	@Override
	@Nullable
	public RPsiElement getValue()
	{
		return getChildByType(RPsiElement.class, getLabel() != null ? 0 : 1);
	}

	@Override
	@Nullable
	public String getKeyName()
	{
		final PsiElement label = getLabel();
		if(label != null)
		{
			return StringUtil.trimEnd(label.getText(), ":");
		}
		final RPsiElement key = getKey();
		if(key == null)
		{
			return null;
		}
		if(key instanceof RSymbol || key instanceof RStringLiteral)
		{
			return ParamDefImplUtil.getElementText(key);
		}
		return key.getText();
	}

	@Nullable
	private PsiElement getLabel()
	{
		final PsiElement first = getFirstChild();
		return first != null && first.getNode().getElementType() == RubyTokenTypes.tLABEL ? first : null;
	}
}