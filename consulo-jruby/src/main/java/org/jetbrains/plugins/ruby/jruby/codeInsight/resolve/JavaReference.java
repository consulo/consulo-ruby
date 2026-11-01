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
import consulo.document.util.TextRange;
import consulo.ruby.interop.ForeignReference;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiPackage;
import consulo.language.psi.PsiReference;
import consulo.language.util.IncorrectOperationException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.ResolveUtil;
import org.jetbrains.plugins.ruby.ruby.lang.findUsages.RubyUsageType;
import org.jetbrains.plugins.ruby.ruby.lang.findUsages.RubyUsageTypeProvider;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.basicTypes.stringLiterals.RBaseString;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Sep 12, 2007
 */
public class JavaReference implements ForeignReference
{
	private TextRange myTextRange;
	private String myToken;
	private JavaReference myJavaReference;
	private PsiManager psiManager;
	private RPsiElement myElement;
	private RBaseString myString;

	public JavaReference(@Nullable final JavaReference javaReference, @Nonnull final RPsiElement element, @Nonnull final RBaseString string, final int index, @Nonnull final String token)
	{
		psiManager = PsiManager.getInstance(element.getProject());
		myElement = element;
		myString = string;
		myJavaReference = javaReference;
		myTextRange = new TextRange(index, index + token.length());
		myToken = token;
	}

	@Override
	public PsiElement getElement()
	{
		return myElement;
	}

	@Override
	public TextRange getRangeInElement()
	{
		return myTextRange;
	}

	@Override
	@Nullable
	public PsiElement resolve()
	{
		final PsiElement element = resolveParent();
		if(element instanceof PsiJavaPackage)
		{
			final PsiJavaPackage packaggge = (PsiJavaPackage) element;
			for(PsiJavaPackage subPackage : packaggge.getSubPackages())
			{
				if(myToken.equals(subPackage.getName()))
				{
					RubyUsageTypeProvider.setType(JavaReference.this, RubyUsageType.UNCLASSIFIED);
					return subPackage;
				}
			}
			for(PsiClass subClass : packaggge.getClasses())
			{
				if(myToken.equals(subClass.getName()))
				{
					RubyUsageTypeProvider.setType(JavaReference.this, RubyUsageType.UNCLASSIFIED);
					return subClass;
				}
			}
		}
		if(element instanceof PsiClass)
		{
			final PsiClass clazzzz = (PsiClass) element;
			for(PsiClass subClass : clazzzz.getAllInnerClasses())
			{
				if(myToken.equals(subClass.getName()))
				{
					RubyUsageTypeProvider.setType(JavaReference.this, RubyUsageType.UNCLASSIFIED);
					return subClass;
				}
			}
		}
		return null;
	}

	@Nullable
	public PsiElement resolveParent()
	{
		return myJavaReference != null ? myJavaReference.resolve() : JavaPsiFacade.getInstance(myElement.getProject()).findPackage("");
	}

	@Override
	public String getCanonicalText()
	{
		return myJavaReference != null ? myJavaReference.getCanonicalText() + '.' + myToken : myToken;
	}

	@Override
	public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException
	{
		return null;
	}

	// IDEA calls bindToElement if we rename/move Java class
	@Override
	public PsiElement bindToElement(@Nonnull PsiElement element) throws IncorrectOperationException
	{
		if(element instanceof PsiClass)
		{
			final String name = ((PsiClass) element).getQualifiedName();
			final RPsiElement new_element = RubyPsiUtil.getTopLevelElements(myElement.getProject(), "\"" + name + "\"").get(0);
			RubyPsiUtil.replaceInParent(myString, new_element);
			return new_element;
		}
		return null;
	}

	@Override
	public boolean isReferenceTo(PsiElement element)
	{
		return ResolveUtil.isReferenceTo(this, element);
	}

	@Override
	public Object[] getVariants()
	{
		final ArrayList<PsiElement> variants = new ArrayList<PsiElement>();
		final PsiElement element = resolveParent();
		if(element instanceof PsiJavaPackage)
		{
			final PsiJavaPackage packagggge = (PsiJavaPackage) element;
			variants.addAll(Arrays.asList(packagggge.getSubPackages()));
			variants.addAll(Arrays.asList(packagggge.getClasses()));
		}
		if(element instanceof PsiClass)
		{
			final PsiClass clazzzz = (PsiClass) element;
			variants.addAll(Arrays.asList(clazzzz.getAllInnerClasses()));
		}
		return variants.toArray(new Object[variants.size()]);
	}

	@Override
	public boolean isSoft()
	{
		return true;
	}

	@Nonnull
	public String getName()
	{
		return myToken;
	}

	@Nullable
	public String getLocation()
	{
		final PsiElement element = resolveParent();
		if(element instanceof PsiPackage)
		{
			return ((PsiPackage) element).getQualifiedName();
		}
		if(element instanceof PsiClass)
		{
			return ((PsiClass) element).getQualifiedName();
		}
		return null;
	}
}
