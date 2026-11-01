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

package org.jetbrains.plugins.ruby.jruby.codeInsight.resolve;

import com.intellij.java.language.psi.JavaPsiFacade;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiJavaPackage;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiPackage;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Sep 13, 2007
 */
public class JavaResolveUtil
{
	public static final String[] TOP_LEVEL_JAVA_ALLOWED = new String[]{
			"java",
			"javax",
			"org",
			"com"
	};

	public static boolean isTopLevelPackageOk(@Nonnull final PsiPackage packaggge)
	{
		final String name = packaggge.getName();
		for(String s : TOP_LEVEL_JAVA_ALLOWED)
		{
			if(s.equals(name))
			{
				return true;
			}
		}
		return false;
	}

	@Nonnull
	public static List<PsiElement> getTopLevelPackagesAndClasses(@Nonnull final Project project)
	{
		final ArrayList<PsiElement> list = new ArrayList<PsiElement>();
		final PsiJavaPackage topLevel = JavaPsiFacade.getInstance(project).findPackage("");
		if(topLevel != null)
		{
			// Add ALL top level packages
			for(PsiPackage psiPackage : topLevel.getSubPackages())
			{
				if(psiPackage.isValid())
				{
					list.add(psiPackage);
				}
			}
			// Add top level classes
			for(PsiClass psiClass : topLevel.getClasses())
			{
				if(psiClass.isValid())
				{
					list.add(psiClass);
				}
			}
		}
		return list;
	}

	@Nonnull
	public static List<PsiElement> getAllowedTopLevelPackagesAndClasses(@Nonnull final Project project)
	{
		final ArrayList<PsiElement> list = new ArrayList<PsiElement>();
		// Add ALLOWED top level packages
		for(String name : TOP_LEVEL_JAVA_ALLOWED)
		{
			final PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(name);
			if(psiPackage != null && psiPackage.isValid())
			{
				list.add(psiPackage);
			}
		}

		// Add top level classes
		final PsiJavaPackage topLevel = JavaPsiFacade.getInstance(project).findPackage("");
		if(topLevel != null && topLevel.isValid())
		{
			for(PsiClass psiClass : topLevel.getClasses())
			{
				if(psiClass.isValid())
				{
					list.add(psiClass);
				}
			}
		}
		return list;
	}

	@Nullable
	public static PsiElement getPackageOrClass(@Nonnull final Project project, @Nonnull final String fullName)
	{
		final PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(fullName);
		if(psiPackage != null && psiPackage.isValid())
		{
			return psiPackage;
		}
		final PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(fullName, GlobalSearchScope.allScope(project));
		if(psiClass != null && psiClass.isValid())
		{
			return psiClass;
		}
		return null;
	}

	@Nullable
	public static PsiElement getPackageOrClass(@Nonnull final Project project, @Nonnull final List<String> path)
	{
		final StringBuffer buffer = new StringBuffer();
		for(String name : path)
		{
			if(buffer.length() > 0)
			{
				buffer.append('.');
			}
			buffer.append(name);
		}
		return getPackageOrClass(project, buffer.toString());
	}
}
