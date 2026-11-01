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

package org.jetbrains.plugins.ruby.ruby.lang.structure;

import consulo.codeEditor.Editor;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import consulo.language.editor.structureView.TextEditorBasedStructureViewModel;
import consulo.language.psi.PsiFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RObjectClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RSingletonMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.modules.RModule;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.fileEditor.structureView.tree.Filter;
import consulo.fileEditor.structureView.tree.Grouper;
import consulo.fileEditor.structureView.tree.Sorter;

public class RubyStructureViewModel extends TextEditorBasedStructureViewModel
{
	private static final Class SUITABLE_CLASSES[] = new Class[]{
			RFile.class,
			RModule.class,
			RClass.class,
			RObjectClass.class,
			RMethod.class,
			RSingletonMethod.class
	};


	private RPsiElement myRoot;

	public RubyStructureViewModel(@Nullable final Editor editor, final RPsiElement root)
	{
		// with the editor given, the model does not look for one in the focused component
		super(editor, root.getContainingFile());
		myRoot = root;
	}

	@Override
	@Nonnull
	public StructureViewTreeElement getRoot()
	{
		return new RubyStructureViewElement(myRoot);
	}

	@Override
	@Nonnull
	public Grouper[] getGroupers()
	{
		return Grouper.EMPTY_ARRAY;
	}

	@Override
	@Nonnull
	public Sorter[] getSorters()
	{
		return new Sorter[]{Sorter.ALPHA_SORTER};
	}

	@Override
	@Nonnull
	public Filter[] getFilters()
	{
		return Filter.EMPTY_ARRAY;
	}

	@Override
	protected PsiFile getPsiFile()
	{
		return myRoot.getContainingFile();
	}

	@Override
	@Nonnull
	protected Class[] getSuitableClasses()
	{
		return SUITABLE_CLASSES;
	}
}
