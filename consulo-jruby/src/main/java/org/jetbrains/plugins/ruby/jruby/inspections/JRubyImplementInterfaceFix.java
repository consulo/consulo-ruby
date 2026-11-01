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

package org.jetbrains.plugins.ruby.jruby.inspections;

import consulo.application.Application;
import consulo.codeEditor.Editor;
import consulo.fileEditor.FileEditorManager;
import consulo.language.editor.generation.ImplementMethodHandler;
import consulo.language.editor.inspection.LocalQuickFix;
import consulo.language.editor.inspection.ProblemDescriptor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.navigation.OpenFileDescriptorFactory;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.ruby.lang.RubyLanguage;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Feb 29, 2008
 */
public class JRubyImplementInterfaceFix implements LocalQuickFix
{
	protected PsiElement myEndElement;

	public JRubyImplementInterfaceFix(@Nonnull final PsiElement endElement)
	{
		myEndElement = endElement;
	}

	@Override
	@Nonnull
	public LocalizeValue getName()
	{
		return LocalizeValue.localizeTODO("Implement methods");
	}

	@Override
	public boolean startInWriteAction()
	{
		return false;
	}

	@Override
	@RequiredUIAccess
	public void applyFix(@Nonnull final Project project, @Nonnull final ProblemDescriptor descriptor)
	{
		final ImplementMethodHandler handler = Application.get().getExtensionPoint(ImplementMethodHandler.class).getOrBuildCache(ImplementMethodHandler.KEY).get(RubyLanguage.INSTANCE);
		final PsiFile file = myEndElement.getContainingFile();
		final VirtualFile virtualFile = file != null ? file.getVirtualFile() : null;
		if(handler == null || virtualFile == null)
		{
			return;
		}
		final Editor editor = FileEditorManager.getInstance(project).openTextEditor(OpenFileDescriptorFactory.getInstance(project).builder(virtualFile).offset(myEndElement.getTextOffset()).build(), true);
		if(editor != null && handler.isValidFor(editor, file))
		{
			handler.invoke(project, editor, file);
		}
	}
}
