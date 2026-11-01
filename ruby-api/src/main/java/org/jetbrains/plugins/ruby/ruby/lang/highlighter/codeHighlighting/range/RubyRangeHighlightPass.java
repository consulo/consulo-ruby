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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import jakarta.annotation.Nonnull;

import consulo.application.ApplicationManager;
import org.jetbrains.plugins.ruby.HighlightPassConstants;
import org.jetbrains.plugins.ruby.ruby.lang.highlighter.RubyHighlightInfoType;
import org.jetbrains.plugins.ruby.ruby.lang.highlighter.codeHighlighting.AbstractRubyHighlighterPass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.basicTypes.RSymbol;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.AccessModifier;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures.AccessModifiersUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.references.RReferenceNavigator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RConstant;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RIdentifier;
import org.jetbrains.plugins.ruby.ruby.lang.psi.visitors.RubySystemCallVisitor;
import consulo.language.editor.rawHighlight.HighlightInfo;
import consulo.codeEditor.Editor;
import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.ProgressManager;
import consulo.project.Project;
import consulo.language.psi.PsiElement;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Jun 25, 2007
 */
public class RubyRangeHighlightPass extends AbstractRubyHighlighterPass
{
	private Collection<HighlightInfo> myRangeHighlighters = Collections.emptyList();

	public RubyRangeHighlightPass(@Nonnull final Project project, @Nonnull final RFile psiFile, @Nonnull final Editor editor)
	{
		super(project, psiFile, editor, true, HighlightPassConstants.RUBY_HIGHLIGHTER_RANGE_GROUP);
	}

	@Override
	public void doCollectInformation(final ProgressIndicator progress)
	{
		ApplicationManager.getApplication().assertReadAccessAllowed();
		myRangeHighlighters = collectRangeInfos();
	}

	@Override
	public void doApplyInformationToEditor()
	{
		RubyRangeHighlightingUtil.setHighlightInfosToEditor(myProject, myDocument, myRangeHighlighters);
	}

	private Collection<HighlightInfo> collectRangeInfos()
	{
		final HashSet<HighlightInfo> highlightInfos = new HashSet<HighlightInfo>();
		final RangeHighlightingVisitor myVisitor = new RangeHighlightingVisitor(highlightInfos);
		for(PsiElement element : getElementsInRange())
		{
			// Hope it`s often enough
			ProgressManager.getInstance().checkCanceled();
			element.accept(myVisitor);
		}
		return highlightInfos;
	}

	class RangeHighlightingVisitor extends RubySystemCallVisitor
	{
		private Collection<HighlightInfo> myCollection;

		public RangeHighlightingVisitor(final Collection<HighlightInfo> collection)
		{
			myCollection = collection;
		}

		@Override
		public void visitRConstant(RConstant rConstant)
		{
			if(rConstant.isRealConstant())
			{
				myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_CONSTANT_DEF_HIGHLIGHT).range(rConstant).create());
			}
		}

		@Override
		public void visitRIdentifier(RIdentifier rIdentifier)
		{
			if(rIdentifier.isParameter() || rIdentifier.isLocalVariable())
			{
				myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_LOCAL_VARIABLE_HIGHTLIGHT).range(rIdentifier).create());
				return;
			}
			// If its not a command
			if(RReferenceNavigator.getReferenceByRightPart(rIdentifier) == null)
			{
				final String text = rIdentifier.getText();
				AccessModifier mod = AccessModifiersUtil.getModifierByName(text);
				if(mod == AccessModifier.PUBLIC)
				{
					myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_PUBLIC_CALL_HIGHLIGHT).range(rIdentifier).create());
				}
				if(mod == AccessModifier.PRIVATE)
				{
					myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_PRIVATE_CALL_HIGHLIGHT).range(rIdentifier).create());
				}
				if(mod == AccessModifier.PROTECTED)
				{
					myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_PROTECTED_CALL_HIGHLIGHT).range(rIdentifier).create());
				}
				// commands
				if(RCall.ATTR_ACCESSOR_COMMAND.equals(text))
				{
					myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_ATTR_ACCESSOR_CALL_HIGHLIGHT).range(rIdentifier).create());
				}
				if(RCall.ATTR_READER_COMMAND.equals(text))
				{
					myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_ATTR_READER_CALL_HIGHLIGHT).range(rIdentifier).create());
				}
				if(RCall.ATTR_WRITER_COMMAND.equals(text))
				{
					myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_ATTR_WRITER_CALL_HIGHLIGHT).range(rIdentifier).create());
				}

				if(RCall.EXTEND_COMMAND.equals(text) || RCall.INCLUDE_COMMAND.equals(text))
				{
					myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_INCLUDE_OR_EXTEND_CALL_HIGHLIGHT).range(rIdentifier).create());
				}

				final RFile rFile = RubyPsiUtil.getRFile(rIdentifier);
				assert rFile != null;
				if(rFile.isInteropEnabled() && (RCall.IMPORT_COMMAND.equals(text) || RCall.JAVA_IMPORT_COMMAND.equals(text) ||
						RCall.INCLUDE_CLASS_COMMAND.equals(text) ||
						RCall.INCLUDE_PACKAGE_COMMAND.equals(text)))
				{
					myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.JRUBY_INCLUDE_JAVA_HIGHTLIGHT).range(rIdentifier).create());
				}

				if(RCall.LOAD_COMMAND.equals(text) || RCall.REQUIRE_COMMAND.equals(text))
				{
					myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_REQUIRE_OR_LOAD_CALL_HIGHLIGHT).range(rIdentifier).create());
				}

				if(RCall.GEM_COMMAND.equals(text) || RCall.REQUIRE_GEM_COMMAND.equals(text))
				{
					myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_REQUIRE_OR_LOAD_CALL_HIGHLIGHT).range(rIdentifier).create());
				}
			}
		}

		@Override
		public void visitRequireCall(@Nonnull RCall rCall)
		{
			visitRequireOrLoad(rCall);
		}

		@Override
		public void visitLoadCall(@Nonnull RCall rCall)
		{
			visitRequireOrLoad(rCall);
		}

		private void visitRequireOrLoad(@Nonnull RCall rCall)
		{
			myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_REQUIRE_OR_LOAD_CALL_HIGHLIGHT).range(rCall.getPsiCommand()).create());
			for(RPsiElement arg : rCall.getArguments())
			{
				myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_REQUIRE_OR_LOAD_CALL_ARG_HIGHLIGHT).range(arg).create());
			}
		}

		@Override
		public void visitIncludeCall(@Nonnull RCall rCall)
		{
			visitIncludeOrExtend(rCall);
		}

		@Override
		public void visitExtendCall(@Nonnull RCall rCall)
		{
			visitIncludeOrExtend(rCall);
		}

		private void visitIncludeOrExtend(RCall rCall)
		{
			myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_INCLUDE_OR_EXTEND_CALL_HIGHLIGHT).range(rCall.getPsiCommand()).create());
		}

		@Override
		public void visitAttrAccessorCall(@Nonnull RCall rCall)
		{
			myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_ATTR_ACCESSOR_CALL_HIGHLIGHT).range(rCall.getPsiCommand()).create());
		}

		@Override
		public void visitAttrWriterCall(@Nonnull RCall rCall)
		{
			myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_ATTR_WRITER_CALL_HIGHLIGHT).range(rCall.getPsiCommand()).create());
		}

		@Override
		public void visitAttrReaderCall(@Nonnull RCall rCall)
		{
			myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_ATTR_READER_CALL_HIGHLIGHT).range(rCall.getPsiCommand()).create());
		}

		@Override
		public void visitAttrInternalCall(@Nonnull RCall rCall)
		{
			myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_RAILS_ATTR_CALL_HIGHLIGHT).range(rCall.getPsiCommand()).create());
		}

		@Override
		public void visitCAttrAccessorCall(@Nonnull final RCall rCall)
		{
			myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_RAILS_ATTR_CALL_HIGHLIGHT).range(rCall.getPsiCommand()).create());
		}

		@Override
		public void visitPublicCall(@Nonnull RCall rCall)
		{
			myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_PUBLIC_CALL_HIGHLIGHT).range(rCall.getPsiCommand()).create());
		}

		@Override
		public void visitProtectedCall(@Nonnull RCall rCall)
		{
			myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_PROTECTED_CALL_HIGHLIGHT).range(rCall.getPsiCommand()).create());
		}

		@Override
		public void visitPrivateCall(@Nonnull RCall rCall)
		{
			myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_PRIVATE_CALL_HIGHLIGHT).range(rCall.getPsiCommand()).create());
		}

		@Override
		public void visitRSymbol(@Nonnull RSymbol rSymbol)
		{
			myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_SYMBOL_HIGHLIGHT).range(rSymbol).create());
		}

		@Override
		public void visitImportClassCall(@Nonnull final RCall rCall)
		{
			final RFile rFile = RubyPsiUtil.getRFile(rCall);
			assert rFile != null;
			if(rFile.isInteropEnabled())
			{
				myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.JRUBY_INCLUDE_JAVA_HIGHTLIGHT).range(rCall.getPsiCommand()).create());
			}
		}

		@Override
		public void visitIncludeClassCall(@Nonnull RCall rCall)
		{
			final RFile rFile = RubyPsiUtil.getRFile(rCall);
			assert rFile != null;
			if(rFile.isInteropEnabled())
			{
				myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.JRUBY_INCLUDE_JAVA_HIGHTLIGHT).range(rCall.getPsiCommand()).create());
			}
		}

		@Override
		public void visitIncludePackageCall(@Nonnull RCall rCall)
		{
			final RFile rFile = RubyPsiUtil.getRFile(rCall);
			assert rFile != null;
			if(rFile.isInteropEnabled())
			{
				myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.JRUBY_INCLUDE_JAVA_HIGHTLIGHT).range(rCall.getPsiCommand()).create());
			}
		}

		@Override
		public void visitGemCall(@Nonnull RCall rCall)
		{
			myCollection.add(HighlightInfo.newHighlightInfo(RubyHighlightInfoType.RUBY_REQUIRE_GEM_CALL_HIGHLIGHT).range(rCall.getPsiCommand()).create());
		}
	}
}
