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

package org.jetbrains.plugins.ruby.ruby.inspections.resolve;

import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.codeInsight.references.RQualifiedReference;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.ResolveUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.LastSymbolStorage;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.inspections.RubyInspectionVisitor;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RBinaryExpression;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RBoolBinExpression;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RBoolNegExpression;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RExpression;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RRangeExpression;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RUnaryExpression;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.RPsiElementBase;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.methodCall.RCallNavigator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.references.RReferenceBase;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.references.RReferenceNavigator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.references.RReference;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RConstant;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RFid;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RIdentifier;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.global.RGlobalVariable;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.application.progress.ProgressManager;
import consulo.language.psi.PsiReference;
import consulo.language.psi.ResolveResult;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Jun 13, 2007
 */
public class RubyResolveVisitor extends RubyInspectionVisitor
{

	public RubyResolveVisitor(@Nonnull final ProblemsHolder holder)
	{
		super(holder);
	}


	@Override
	public void visitRConstant(@Nonnull final RConstant rConstant)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		if(!rConstant.isInDefinition() &&
				shouldInspectElement(rConstant) &&
				RCallNavigator.getByCommand(rConstant) == null)
		{
			if(ResolveUtil.resolveToSymbols(rConstant).isEmpty())
			{
				registerProblem(rConstant, RBundle.message("inspection.resolve.cannot.find", rConstant.getText()));
			}
		}
	}

	@Override
	public void visitRFid(@Nonnull final RFid rFid)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		if(shouldInspectElement(rFid) && ResolveUtil.resolveToSymbols(rFid).isEmpty())
		{
			registerProblem(rFid, RBundle.message("inspection.resolve.cannot.find", rFid.getText()));
		}
	}

	@Override
	public void visitRIdentifier(@Nonnull final RIdentifier rIdentifier)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		if(!shouldInspectElement(rIdentifier) || rIdentifier.isParameter() || rIdentifier.isLocalVariable())
		{
			return;
		}
		final String text = rIdentifier.getText();
		// We shouldn`t inspect these built-in commands, because they are missed in stubs
		if(RCall.GEM_COMMAND.equals(text) || RCall.REQUIRE_GEM_COMMAND.equals(text) ||
				RCall.IMPORT_COMMAND.equals(text) || RCall.JAVA_IMPORT_COMMAND.equals(text) || RCall.INCLUDE_CLASS_COMMAND.equals(text) || RCall.INCLUDE_PACKAGE_COMMAND.equals(text))
		{
			return;
		}
		if(ResolveUtil.resolveToSymbols(rIdentifier).isEmpty())
		{
			registerProblem(rIdentifier, RBundle.message("inspection.resolve.cannot.find", text));
		}
	}

	@Override
	public void visitRGlobalVariable(@Nonnull final RGlobalVariable globalVariable)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		if(shouldInspectElement(globalVariable) && ResolveUtil.resolveToSymbols(globalVariable).isEmpty())
		{
			registerProblem(globalVariable, RBundle.message("inspection.resolve.cannot.find", globalVariable.getText()));
		}
	}


	@Override
	public void visitRReference(@Nonnull final RReference rReference)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		// we shouldn`t inspect it, because class can have no constructor
		if(((RReferenceBase) rReference).isConstructorLike() || !shouldInspectElement(rReference))
		{
			return;
		}

		// we shouldn`t check if reciever isn`t typed correctly
		final FileSymbol fileSymbol = LastSymbolStorage.getInstance(rReference.getProject()).getSymbol();
		final RPsiElement element = rReference.getReciever();
		if(element instanceof RExpression && !((RExpression) element).getType(fileSymbol).isTyped())
		{
			return;
		}

		final PsiReference psiReference = rReference.getReference();
		if(psiReference instanceof RQualifiedReference)
		{
			final RQualifiedReference RQualifiedReference = (RQualifiedReference) psiReference;
			final ResolveResult[] results = RQualifiedReference.multiResolve(false);
			if(results.length == 0 || results[0].getElement() == rReference)
			{
				registerProblem(RQualifiedReference.getRefValue(), RBundle.message("inspection.resolve.cannot.find", RQualifiedReference.getCanonicalText()));
			}
		}
	}

	@Override
	public void visitRBinaryExpression(@Nonnull final RBinaryExpression rBinaryExpression)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		// we shouldn`t inspect ranges and boolean bin expressions
		if(rBinaryExpression instanceof RRangeExpression || rBinaryExpression instanceof RBoolBinExpression)
		{
			return;
		}
		if(!shouldInspectElement(rBinaryExpression))
		{
			return;
		}

		// we shouldn`t check if reciever isn`t typed correctly
		final FileSymbol fileSymbol = LastSymbolStorage.getInstance(rBinaryExpression.getProject()).getSymbol();
		final RPsiElement element = rBinaryExpression.getLeftOperand();
		if(element instanceof RExpression && !((RExpression) element).getType(fileSymbol).isTyped())
		{
			return;
		}

		final PsiReference psiReference = rBinaryExpression.getReference();
		assert psiReference instanceof RQualifiedReference;
		final RQualifiedReference RQualifiedReference = (RQualifiedReference) psiReference;
		if(RQualifiedReference.multiResolve(false).length == 0)
		{
			registerProblem(RQualifiedReference.getRefValue(), RBundle.message("inspection.resolve.cannot.find", RQualifiedReference.getCanonicalText()));
		}
	}

	@Override
	public void visitRUnaryExpression(@Nonnull final RUnaryExpression rUnaryExpression)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		// we shouldn`t inspect boolean negative expressions
		if(rUnaryExpression instanceof RBoolNegExpression)
		{
			return;
		}

		// we shouldn`t check if reciever isn`t typed correctly
		final FileSymbol fileSymbol = LastSymbolStorage.getInstance(rUnaryExpression.getProject()).getSymbol();
		final RPsiElement element = rUnaryExpression.getElement();
		if(element instanceof RExpression && !((RExpression) element).getType(fileSymbol).isTyped())
		{
			return;
		}

		final PsiReference psiReference = rUnaryExpression.getReference();
		if(psiReference instanceof RQualifiedReference)
		{
			final RQualifiedReference RQualifiedReference = (RQualifiedReference) psiReference;
			if(RQualifiedReference.multiResolve(false).length == 0)
			{
				registerProblem(RQualifiedReference.getRefValue(), RBundle.message("inspection.resolve.cannot.find", RQualifiedReference.getCanonicalText()));
			}
		}
	}

	/**
	 * Should we inspect element
	 *
	 * @param element element to check
	 * @return true if we shoud inspect, false otherwise
	 */
	private boolean shouldInspectElement(@Nonnull final RPsiElement element)
	{
		return RReferenceNavigator.getReferenceByRightPart(element) == null && !((RPsiElementBase) element).isClassOrModuleName();
	}
}
