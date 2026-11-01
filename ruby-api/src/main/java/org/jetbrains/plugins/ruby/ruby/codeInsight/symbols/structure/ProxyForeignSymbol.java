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

package org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure;

import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.lang.documentation.MarkupUtil;
import consulo.language.psi.PsiElement;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Sep 25, 2007
 */
public class ProxyForeignSymbol extends Symbol
{

	private final PsiElement myPsiJavaElement;

	public ProxyForeignSymbol(@Nonnull final FileSymbol fileSymbol, @Nullable final String name, @Nullable final PsiElement element, @Nullable final Symbol parent, @Nullable final RPsiElement prototype)
	{
		super(fileSymbol, name, Type.FOREIGN_PROXY_CLASS, parent, prototype);
		myPsiJavaElement = element;
	}

	@Override
	@SuppressWarnings({"StringConcatenationInsideStringBufferAppend"})
	public String toString(@Nonnull final FileSymbol fileSymbol, final boolean useHtml)
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("[" + getId() + "] " + getType() + " ");
		if(useHtml)
		{
			MarkupUtil.appendBold(builder, myPsiJavaElement.toString());
		}
		else
		{
			builder.append(myPsiJavaElement);
		}
		return builder.toString();
	}

	@Nonnull
	public PsiElement getPsiElement()
	{
		return myPsiJavaElement;
	}
}
