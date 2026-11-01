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

package org.jetbrains.plugins.ruby.ruby.lang.highlighter.codeHighlighting.line;

import consulo.language.psi.PsiElement;
import consulo.ruby.interop.RubyInterop;
import consulo.ruby.interop.RubyInteropProvider;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RStructuralElement;

import consulo.codeEditor.markup.GutterIconRenderer;
import consulo.project.Project;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.RubyIcons;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.lang.documentation.MarkupConstants;
import org.jetbrains.plugins.ruby.ruby.lang.documentation.MarkupUtil;
import org.jetbrains.plugins.ruby.ruby.presentation.RContainerPresentationUtil;

import java.awt.event.MouseEvent;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Jun 27, 2007
 */
public class RubyGutterInfo extends RubyLineMarkerInfo
{
	enum Mode
	{
		OVERRIDE,
		IMPLEMENT
	}

	private final Mode myMode;
	private final List myElements;
	private final Project myProject;
	private final Symbol mySymbol;
	private String myToolTip;
	private static final String NEWLINE_AND_SPACES = "<br>&nbsp;&nbsp;&nbsp;";

	public RubyGutterInfo(final Mode mode, @Nonnull final Project project, @Nonnull final Symbol symbol, @Nonnull final List elements, int startOffset)
	{
		super(startOffset, true);
		myMode = mode;
		myProject = project;
		mySymbol = symbol;
		myElements = elements;
		assert !myElements.isEmpty();

		createToolTip();
	}

	private void createToolTip()
	{
		final StringBuilder buffer = new StringBuilder();
		buffer.append(myMode == Mode.OVERRIDE ? RBundle.message("overrides") : RBundle.message("implements"));
		buffer.append(MarkupConstants.SPACE);
		MarkupUtil.appendBoldCode(buffer, mySymbol.getName());
		buffer.append(MarkupConstants.SPACE).append(RBundle.message("in"));

		final LinkedHashSet<String> locations = new LinkedHashSet<String>();
		for(Object element : myElements)
		{
			// Here we process Ruby elements
			if(element instanceof RStructuralElement)
			{
				locations.add(RContainerPresentationUtil.getLocation((RStructuralElement) element));
			}
			// Here we process overriden foreign methods
			else if(element instanceof PsiElement foreignElement)
			{
				final RubyInteropProvider provider = RubyInterop.forElement(foreignElement);
				final String location = provider != null ? provider.getContainingClassName(foreignElement) : null;
				if(location != null)
				{
					locations.add(location);
				}
			}
		}

		final int size = locations.size();
		for(String location : locations)
		{
			if(size > 1)
			{
				buffer.append(NEWLINE_AND_SPACES);
			}
			else
			{
				buffer.append(MarkupConstants.SPACE);
			}
			buffer.append(location);
		}

		myToolTip = buffer.toString();
	}

	public Mode getMode()
	{
		return myMode;
	}

	public List getElements()
	{
		return myElements;
	}

	public Project getProject()
	{
		return myProject;
	}

	@Override
	@Nullable
	public GutterIconRenderer createGutterRenderer()
	{
		return new MyGutterIconRenderer();
	}

	private class MyGutterIconRenderer extends GutterIconRenderer
	{
		@Override
		@Nonnull
		public Image getIcon()
		{
			return myMode == Mode.OVERRIDE ? RubyIcons.RUBY_GUTTER_OVERRIDING : RubyIcons.RUBY_GUTTER_IMPLEMENTING;
		}

		@Override
		public AnAction getClickAction()
		{
			return new MyNavigateAction();
		}

		@Override
		public boolean isNavigateAction()
		{
			return true;
		}

		@Override
		public String getTooltipText()
		{
			return myToolTip;
		}

		@Override
		public GutterIconRenderer.Alignment getAlignment()
		{
			return Alignment.LEFT;
		}

		@Override
		public boolean equals(Object o)
		{
			return false;
		}

		@Override
		public int hashCode()
		{
			return 0;
		}
	}

	private class MyNavigateAction extends AnAction
	{
		@Override
		public void actionPerformed(final AnActionEvent e)
		{
			MouseEvent mouseEvent = (MouseEvent) e.getInputEvent();
			RubyGutterNavigator.browse(mouseEvent, RubyGutterInfo.this);
		}
	}
}
