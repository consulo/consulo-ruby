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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import consulo.language.psi.PsiElement;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Sep 13, 2007
 */
/**
 * A symbol for an element of another language, e.g. a Java class or method used from JRuby.
 */
public class ForeignSymbol extends PsiElementSymbol
{
	private String myAlternativeName;

	public ForeignSymbol(@Nonnull final PsiElement element, @Nonnull final String realName, @Nullable final String alternativeName, final Type type)
	{
		super(element, realName, type);
		myAlternativeName = alternativeName;
	}

	@Nullable
	public String getAlternativeName()
	{
		return myAlternativeName;
	}
}
