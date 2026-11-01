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

import jakarta.annotation.Nonnull;

import consulo.codeEditor.Editor;
import consulo.fileEditor.structureView.StructureViewModel;
import consulo.fileEditor.structureView.TreeBasedStructureViewBuilder;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import consulo.language.psi.PsiFile;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 01.08.2006
 */
public class RubyStructureViewBuilder extends TreeBasedStructureViewBuilder
{
	private PsiFile myPsiFile;

	public RubyStructureViewBuilder(@Nonnull final PsiFile psiFile)
	{
		myPsiFile = psiFile;
	}

	@Override
	@Nonnull
	public StructureViewModel createStructureViewModel(Editor editor)
	{
		return new RubyStructureViewModel(editor, (RPsiElement) myPsiFile);
	}
}
