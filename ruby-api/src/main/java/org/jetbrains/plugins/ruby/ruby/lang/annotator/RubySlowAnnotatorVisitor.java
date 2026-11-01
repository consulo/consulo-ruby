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

package org.jetbrains.plugins.ruby.ruby.lang.annotator;

import consulo.ruby.interop.ForeignReference;
import consulo.ruby.interop.RubyInterop;
import consulo.ruby.interop.RubyInteropProvider;
import consulo.application.progress.ProgressManager;
import consulo.language.editor.annotation.Annotation;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.ResolveResult;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.codeInsight.references.RFieldAttrReference;
import org.jetbrains.plugins.ruby.ruby.codeInsight.references.RFileReference;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.ResolveUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.lang.highlighter.RubyHighlighterKeys;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.basicTypes.RSymbol;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.holders.utils.RFileUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.visitors.RubySystemCallVisitor;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Mar 6, 2007
 */
public class RubySlowAnnotatorVisitor extends RubySystemCallVisitor
{
	private AnnotationHolder myHolder;
	private RFile myFile;


	public RubySlowAnnotatorVisitor(@Nonnull final AnnotationHolder holder, @Nonnull final RFile rFile)
	{
		myHolder = holder;
		myFile = rFile;
	}

	// required files annotating
	@Override
	public void visitRequireCall(@Nonnull final RCall rCall)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		visitRequireOrLoad(rCall);
	}

	@Override
	public void visitLoadCall(@Nonnull RCall rCall)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		visitRequireOrLoad(rCall);
	}

	private void visitRequireOrLoad(RCall rCall)
	{
		final List<RPsiElement> args = rCall.getArguments();
		if(args.isEmpty())
		{
			myHolder.createErrorAnnotation(rCall, RBundle.message("annotation.error.no.arguments"));
			return;
		}
		final PsiReference[] requiredReferences = rCall.getReferences();
		for(PsiReference reference : requiredReferences)
		{
			if(reference instanceof RFileReference)
			{
				final RFileReference reqReference = (RFileReference) reference;
				final PsiElement ref = reqReference.getRefValue();
				final String name = RFileUtil.evaluate(rCall.getContainingFile().getVirtualFile(), ref);
				if(!(myFile.isInteropEnabled() && name != null && isInteropLibrary(name)))
				{
					if(name != null)
					{
						final ResolveResult[] results = reqReference.multiResolve(true);
						if(results.length == 0)
						{
							myHolder.createErrorAnnotation(ref, RBundle.message("annotation.error.cannot.find.required.file"));
						}
						if(results.length > 1)
						{
							final Annotation annotation = myHolder.createWarningAnnotation(ref, RBundle.message("annotation.warning.implicit.multivariant.required.item"));
							annotation.setTextAttributes(RubyHighlighterKeys.INSPECTION_MULTIPLE_RESOLVE_WARNING);
						}
					}
					else
					{
						myHolder.createWarningAnnotation(ref, RBundle.message("annotation.warning.implicit.required.item"));
					}
				}
			}
		}
	}

	@Override
	public void visitIncludeCall(@Nonnull RCall rCall)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		visitIncludeOrExtend(rCall);
	}

	@Override
	public void visitExtendCall(@Nonnull RCall rCall)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		visitIncludeOrExtend(rCall);
	}

	private void visitIncludeOrExtend(RCall rCall)
	{
		final List<RPsiElement> args = rCall.getArguments();
		if(args.isEmpty())
		{
			myHolder.createErrorAnnotation(rCall, RBundle.message("annotation.error.no.arguments"));
			return;
		}
		for(RPsiElement arg : args)
		{
			final List<Symbol> symbols = ResolveUtil.resolveToSymbols(arg);
			if(symbols.size() != 1)
			{
				myHolder.createWarningAnnotation(arg, RBundle.message("annotation.warning.cannot.resolve.include"));
			}
		}
	}

	@Override
	public void visitAttrAccessorCall(@Nonnull RCall rCall)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		visitAttrCall(rCall);
	}

	@Override
	public void visitAttrWriterCall(@Nonnull RCall rCall)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		visitAttrCall(rCall);
	}

	@Override
	public void visitAttrReaderCall(@Nonnull RCall rCall)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		visitAttrCall(rCall);
	}

	@Override
	public void visitAttrInternalCall(@Nonnull RCall rCall)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		visitAttrCall(rCall);
	}

	@Override
	public void visitCAttrAccessorCall(@Nonnull final RCall rCall)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		visitAttrCall(rCall);
	}

	private void visitAttrCall(@Nonnull RCall rCall)
	{
		final List<RPsiElement> args = rCall.getArguments();
		if(args.isEmpty())
		{
			myHolder.createErrorAnnotation(rCall, RBundle.message("annotation.error.no.arguments"));
			return;
		}
		final PsiReference[] fieldReferences = rCall.getReferences();
		for(PsiReference reference : fieldReferences)
		{
			final RFieldAttrReference fieldAttrReference = (RFieldAttrReference) reference;
			final ResolveResult[] results = fieldAttrReference.multiResolve(true);
			final PsiElement ref = fieldAttrReference.getReferenceContent();
			if(results.length == 0)
			{
				myHolder.createWarningAnnotation(ref, RBundle.message("annotation.warning.cannot.find.variable"));
			}
			if(results.length > 1)
			{
				myHolder.createWarningAnnotation(ref, RBundle.message("annotation.warning.multivariable.found"));
			}
		}
	}

	@Override
	public void visitImportClassCall(@Nonnull final RCall rCall)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		final List<RPsiElement> args = rCall.getArguments();
		if(args.isEmpty())
		{
			myHolder.createErrorAnnotation(rCall, RBundle.message("annotation.error.no.arguments"));
			return;
		}
		for(RPsiElement arg : args)
		{
			final List<PsiElement> list = ResolveUtil.multiResolve(arg);
			if(!(list.size() == 1 && isForeignClass(list.get(0))))
			{
				myHolder.createErrorAnnotation(arg, RBundle.message("annotation.error.should.be.java.class"));
			}
		}
	}

	@Override
	public void visitIncludeClassCall(@Nonnull final RCall rCall)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		visitIncludeJava(rCall, this::isForeignClass, RBundle.message("annotation.error.should.be.java.class"));
	}

	@Override
	public void visitIncludePackageCall(@Nonnull final RCall rCall)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		visitIncludeJava(rCall, this::isForeignPackage, RBundle.message("annotation.error.should.be.java.package"));
	}

	private boolean isInteropLibrary(@Nonnull final String name)
	{
		return RubyInterop.compute(provider -> provider.isInteropLibrary(name) ? Boolean.TRUE : null) != null;
	}

	private boolean isForeignClass(@Nullable final PsiElement element)
	{
		final RubyInteropProvider provider = RubyInterop.forElement(element);
		return provider != null && provider.isClass(element);
	}

	private boolean isForeignPackage(@Nullable final PsiElement element)
	{
		final RubyInteropProvider provider = RubyInterop.forElement(element);
		return provider != null && provider.isPackage(element);
	}

	private void visitIncludeJava(final RCall rCall, final Predicate<PsiElement> condition, final String errorMsg)
	{
		final List<RPsiElement> args = rCall.getArguments();
		if(args.isEmpty())
		{
			myHolder.createErrorAnnotation(rCall, RBundle.message("annotation.error.no.arguments"));
			return;
		}
		final PsiReference[] references = rCall.getReferences();
		if(references.length == 0)
		{
			myHolder.createErrorAnnotation(rCall, RBundle.message("annotation.error.should.be.java"));
			return;
		}
		if(!condition.test(references[references.length - 1].resolve()))
		{
			myHolder.createErrorAnnotation(rCall, errorMsg);
		}
	}

	@Override
	public void visitRSymbol(RSymbol rSymbol)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		final PsiReference[] references = rSymbol.getReferences();
		for(PsiReference reference : references)
		{
			if(reference instanceof ForeignReference)
			{
				if(reference.resolve() == null)
				{
					myHolder.createErrorAnnotation(rSymbol, RBundle.message("annotation.error.should.be.java"));
				}
			}
		}
	}
}
