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

package org.jetbrains.plugins.ruby.jruby.codeInsight.types;

import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifier;
import com.intellij.java.language.psi.PsiModifierList;

import jakarta.annotation.Nonnull;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Jan 17, 2008
 */
public class JavaPsiUtil
{

	public static boolean isStaticMethod(@Nonnull final PsiMethod method)
	{
		return method.getModifierList().hasModifierProperty(PsiModifier.STATIC);
	}

	public static boolean isStaticField(@Nonnull final PsiField method)
	{
		final PsiModifierList modifiers = method.getModifierList();
		return modifiers != null && modifiers.hasModifierProperty(PsiModifier.STATIC);
	}

}
