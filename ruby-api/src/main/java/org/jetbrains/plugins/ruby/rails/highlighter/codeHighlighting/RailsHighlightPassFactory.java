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

package org.jetbrains.plugins.ruby.rails.highlighter.codeHighlighting;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.DocumentMarkupModel;
import consulo.codeEditor.Editor;
import consulo.codeEditor.markup.MarkupModel;
import consulo.document.Document;
import consulo.language.editor.Pass;
import consulo.language.editor.highlight.TextEditorHighlightingPass;
import consulo.language.editor.highlight.TextEditorHighlightingPassFactory;
import consulo.language.editor.highlight.TextEditorHighlightingPassFactory.Registrar;
import consulo.language.psi.PsiFile;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.rails.codeInsight.daemon.DaemonCodeAnalyzerUtil;
import org.jetbrains.plugins.ruby.rails.codeInsight.daemon.RailsLineMarkerInfo;
import org.jetbrains.plugins.ruby.rails.facet.RailsFacetUtil;
import org.jetbrains.plugins.ruby.rails.nameConventions.ViewsConventions;
import org.jetbrains.plugins.ruby.support.utils.RubyVirtualFileScanner;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 02.02.2007
 */
@ExtensionImpl
public class RailsHighlightPassFactory implements TextEditorHighlightingPassFactory
{
	@Override
	public void register(@Nonnull Registrar registrar)
	{
		registrar.registerTextEditorHighlightingPass(this, Registrar.Anchor.LAST, Pass.UPDATE_ALL, true);
	}

	@Override
	@Nullable
	public TextEditorHighlightingPass createHighlightingPass(final @Nullable PsiFile psiFile, @Nonnull final Editor editor)
	{
		if(psiFile == null)
		{
			return null;
		}

		final Module module = ModuleUtilCore.findModuleForPsiElement(psiFile);
		if(module != null && RailsFacetUtil.hasRailsSupport(module))
		{
			final VirtualFile file = psiFile.getVirtualFile();
			if(file != null)
			{
				final String name = file.getName();
				if(ViewsConventions.isValidViewFileName(name))
				{
					if(ViewsConventions.isPartialViewName(name))
					{
						return new RailsViewToControllerMarkersPass(module, psiFile, editor);
					}
					return new RailsViewToActionMarkersPass(module, psiFile, editor);
				}
				else if(RubyVirtualFileScanner.isRubyFile(file))
				{
					return new RailsControllerToViewMarkersPass(module, psiFile, editor);
				}
			}
		}

		//[HACK] may be it is hack.. Removes all Rails Highlighter if Rails Facet was deleted from module.
		final Document document = editor.getDocument();
		final Project project = psiFile.getProject();
		final RailsLineMarkerInfo[] markers = DaemonCodeAnalyzerUtil.getLineMarkers(document, project);
		//removes all Rails specific markers - the passes are created in the background, the markup model is UI only
		if(markers != null && markers.length > 0)
		{
			project.getApplication().invokeLater(() -> {
				final MarkupModel markupModel = DocumentMarkupModel.forDocument(document, project, false);
				for(RailsLineMarkerInfo marker : markers)
				{
					markupModel.removeHighlighter(marker.highlighter);
				}
				DaemonCodeAnalyzerUtil.setRubyLineMarkers(document, null, project);
			}, project.getDisposed());
		}
		return null;
	}
}

