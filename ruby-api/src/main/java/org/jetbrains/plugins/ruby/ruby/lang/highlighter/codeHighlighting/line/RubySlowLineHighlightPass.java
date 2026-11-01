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

import consulo.application.ApplicationManager;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.ProgressManager;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.HighlightPassConstants;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.LastSymbolStorage;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.RubyOverrideImplementUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.lang.highlighter.codeHighlighting.AbstractRubyHighlighterPass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RVirtualPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Jun 25, 2007
 */
public class RubySlowLineHighlightPass extends AbstractRubyHighlighterPass
{
	private Collection<RubyLineMarkerInfo> myLineMarkers;


	public RubySlowLineHighlightPass(@Nonnull final Project project, @Nonnull final RFile psiFile, @Nonnull final Editor editor)
	{
		super(project, psiFile, editor, false, HighlightPassConstants.RUBY_LINE_MARKERS_GROUP);

		// Force Updating symbol before annotating
		((RFile) myFile).getFileSymbol();
	}

	@Override
	public void doCollectInformation(final ProgressIndicator progress)
	{
		ApplicationManager.getApplication().assertReadAccessAllowed();

		myLineMarkers = new ArrayList<RubyLineMarkerInfo>();
		setOverrideAndImplementMarkers();
	}

	@Override
	public void doApplyInformationToEditor()
	{
		RubyLineHighlightingUtil.setLineMarkersToEditor(myProject, myDocument, myLineMarkers, true);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// Override linemarkers
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void setOverrideAndImplementMarkers()
	{
		for(PsiElement element : getElementsInRange())
		{
			// Hope it`s often enough
			ProgressManager.getInstance().checkCanceled();
			addOverrideOrImplementIfNeeded(element);
		}
	}

	private void addOverrideOrImplementIfNeeded(final PsiElement element)
	{
		if(element instanceof RContainer && !(element instanceof PsiFile))
		{
			final FileSymbol fileSymbol = LastSymbolStorage.getInstance(myProject).getSymbol();
			if(fileSymbol == null)
			{
				return;
			}
			final RContainer virtualContainer = RVirtualPsiUtil.findVirtualContainer((RContainer) element);
			if(virtualContainer == null)
			{
				return;
			}
			final Symbol symbol = SymbolUtil.getSymbolByContainer(fileSymbol, virtualContainer);
			if(symbol == null)
			{
				return;
			}

			final List<Symbol> overridenSymbols = RubyOverrideImplementUtil.getOverridenSymbols(fileSymbol, symbol);
			// Adding ruby override markers
			final List overridenElements = RubyOverrideImplementUtil.getOverridenElements(fileSymbol, symbol, virtualContainer, overridenSymbols);
			if(!overridenElements.isEmpty())
			{
				myLineMarkers.add(new RubyGutterInfo(RubyGutterInfo.Mode.OVERRIDE, element.getProject(), symbol, overridenElements, element.getTextOffset()));
			}
			else
			{
				// We don`t show implement markers if overriden markers list isn`t empty
				// Adding implement markers of foreign methods
				final List<PsiElement> methods = RubyOverrideImplementUtil.getImplementedForeignMethods(overridenSymbols);
				if(!methods.isEmpty())
				{
					myLineMarkers.add(new RubyGutterInfo(RubyGutterInfo.Mode.IMPLEMENT, element.getProject(), symbol, methods, element.getTextOffset()));
				}
			}
		}
	}
}
