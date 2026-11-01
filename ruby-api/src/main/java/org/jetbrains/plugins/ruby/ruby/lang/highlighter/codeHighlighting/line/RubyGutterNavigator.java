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

import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;


import consulo.ide.impl.ui.impl.PopupChooserBuilder;
import consulo.language.editor.ui.DefaultPsiElementCellRenderer;
import consulo.language.psi.PsiElement;
import consulo.logging.Logger;
import consulo.navigation.Navigatable;
import consulo.project.Project;
import consulo.ui.ex.RelativePoint;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RVirtualPsiUtil;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Jun 27, 2007
 */
public class RubyGutterNavigator
{
	private static final Logger LOG = Logger.getInstance(RubyGutterNavigator.class.getName());

	public static void browse(@Nonnull final MouseEvent e, @Nonnull final RubyGutterInfo info)
	{
		final Project project = info.getProject();
		final ArrayList<Navigatable> navigatable = new ArrayList<Navigatable>();
		for(Object element : info.getElements())
		{
			PsiElement psiElem = null;
			if(element instanceof RPsiElement)
			{
				psiElem = RVirtualPsiUtil.findPsiByVirtualElement((RPsiElement) element, project);
			}
			else if(element instanceof PsiElement foreignElement)
			{
				psiElem = foreignElement;
			}
			// Add if we found navigatable element
			if(psiElem instanceof Navigatable)
			{
				navigatable.add((Navigatable) psiElem);
			}
		}
		openTargets(e, info.getMode() == RubyGutterInfo.Mode.OVERRIDE ? RBundle.message("line.marker.override.select.variant") : RBundle.message("line.marker.implement.select.variant"), new DefaultPsiElementCellRenderer(), navigatable.toArray(new Navigatable[navigatable.size()]));
	}

	public static void openTargets(@Nonnull final MouseEvent e, @Nonnull final String title, @Nonnull final ListCellRenderer listRenderer, @Nonnull final Navigatable... targets)
	{
		if(targets.length == 0)
		{
			return;
		}

		if(targets.length == 1)
		{
			targets[0].navigate(true);
			return;
		}

		final JList list = new JList(targets);
		list.setCellRenderer(listRenderer);
		new PopupChooserBuilder(list).setTitle(title).setMovable(true).setItemChoosenCallback(new Runnable()
		{
			@Override
			public void run()
			{
				int[] ids = list.getSelectedIndices();
				if(ids == null || ids.length == 0)
				{
					return;
				}
				Object[] selectedElements = list.getSelectedValues();
				for(Object element : selectedElements)
				{
					PsiElement selected = (PsiElement) element;
					LOG.assertTrue(selected.isValid());
					((Navigatable) selected).navigate(true);
				}
			}
		}).createPopup().show(new RelativePoint(e));
	}
}
