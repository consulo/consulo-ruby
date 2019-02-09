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

package org.jetbrains.plugins.ruby.ruby.lang.highlighter.codeHighlighting.range;

import com.intellij.codeHighlighting.TextEditorHighlightingPass;
import com.intellij.codeHighlighting.TextEditorHighlightingPassFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import javax.annotation.Nonnull;

import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;

import javax.annotation.Nullable;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Jun 26, 2007
 */
public class RubyRangeHighlightPassFactory implements TextEditorHighlightingPassFactory
{
	@Override
	public void register(@Nonnull Registrar registrar)
	{
		registrar.registerTextEditorHighlightingPass(this, null, null, true, -1);
	}

	@Override
	@Nullable
	public TextEditorHighlightingPass createHighlightingPass(final @Nullable PsiFile psiFile, @Nonnull final Editor editor)
	{
		if(psiFile instanceof RFile)
		{
			return new RubyRangeHighlightPass(psiFile.getProject(), (RFile) psiFile, editor);
		}
		return null;
	}
}
