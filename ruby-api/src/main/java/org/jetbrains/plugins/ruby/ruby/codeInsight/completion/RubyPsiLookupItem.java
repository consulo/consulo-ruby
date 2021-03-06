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

package org.jetbrains.plugins.ruby.ruby.codeInsight.completion;

import java.awt.Color;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.plugins.ruby.ruby.cache.psi.RVirtualElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RVirtualPsiUtil;
import com.intellij.codeInsight.lookup.DeferredUserLookupValue;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.codeInsight.lookup.LookupValueWithPriority;
import com.intellij.codeInsight.lookup.LookupValueWithPsiElement;
import com.intellij.codeInsight.lookup.LookupValueWithUIHint;
import com.intellij.codeInsight.lookup.PresentableLookupValue;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import consulo.ui.image.Image;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Apr 22, 2007
 */
public class RubyPsiLookupItem implements RubyLookupItem, PresentableLookupValue, DeferredUserLookupValue, LookupValueWithUIHint, LookupValueWithPsiElement, LookupValueWithPriority, Iconable
{
	private RVirtualElement myPrototype;
	private String myLookupString;
	private String myTypeText;
	private boolean isBold;
	private Image myIcon;
	private Project myProject;
	private String myTailText;
	private PsiElement myCachedPsiElement;
	private int myPriority;

	@Override
	@Nonnull
	public String getName()
	{
		return myLookupString;
	}

	public RubyPsiLookupItem(@Nonnull final Project project, @Nonnull final String lookupString, @Nullable final String tailText, @Nullable final String typeText, @Nonnull final RVirtualElement prototype, final int priority, final boolean bold, @Nullable final Image icon)
	{
		myProject = project;
		myLookupString = lookupString;
		myTailText = tailText;
		myTypeText = typeText;
		myPrototype = prototype;
		myPriority = priority;
		isBold = bold;
		myIcon = icon;
	}

	public boolean isStrikeout()
	{
		return false;
	}

	@Override
	public String getPresentation()
	{
		return myLookupString;
	}

	@Override
	public boolean handleUserSelection(LookupItem item, Project project)
	{
		return true;
	}

	@Override
	public String getTypeHint()
	{
		return myTypeText;
	}

	@Override
	public Color getColorHint()
	{
		return null;
	}

	@Override
	public boolean isBold()
	{
		return isBold;
	}

	@Override
	public int getPriority()
	{
		return myPriority;
	}

	@Override
	public PsiElement getElement()
	{
		if(myPrototype == null)
		{
			return null;
		}
		if(myCachedPsiElement == null)
		{
			// The slowest operation!
			myCachedPsiElement = RVirtualPsiUtil.findPsiByVirtualElement(myPrototype, myProject);
		}
		return myCachedPsiElement;
	}

	@Override
	public Image getIcon(int flags)
	{
		return myIcon;
	}

	public String getTailText()
	{
		return myTailText != null ? myTailText : "";
	}
}
