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

package org.jetbrains.plugins.ruby.jruby.impl.codeInsight.types;

import com.intellij.java.language.psi.PsiMethod;
import jakarta.annotation.Nonnull;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.ruby.jruby.codeInsight.types.JavaPsiUtil;
import org.jetbrains.plugins.ruby.rails.nameConventions.NamingConventions;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Sep 13, 2007
 */
public class JRubyNameConventions
{
	@NonNls
	private static final String GET = "get";

	@NonNls
	private static final String SET = "set";

	@NonNls
	private static final String IS = "is";

	@Nonnull
	public static String getMethodName(@Nonnull final PsiMethod method)
	{
		return getMethodName(method.getName(), !JavaPsiUtil.isStaticMethod(method));
	}

	@Nonnull
	public static String getMethodName(@Nonnull final String name)
	{
		return getMethodName(name, true);
	}

	@Nonnull
	private static String getMethodName(@Nonnull final String name, boolean getsetisEnabled)
	{
		if(getsetisEnabled)
		{
			if(name.startsWith(GET))
			{
				return NamingConventions.toUnderscoreCase(name.substring(GET.length()));
			}
			if(name.startsWith(SET))
			{
				return NamingConventions.toUnderscoreCase(name.substring(SET.length())) + "=";
			}
			if(name.startsWith(IS))
			{
				return NamingConventions.toUnderscoreCase(name.substring(IS.length())) + "?";
			}
		}
		return NamingConventions.toUnderscoreCase(name);
	}
}
