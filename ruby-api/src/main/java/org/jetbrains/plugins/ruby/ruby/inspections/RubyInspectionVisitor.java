package org.jetbrains.plugins.ruby.ruby.inspections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.plugins.ruby.ruby.lang.psi.visitors.RubyElementVisitor;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;

/**
 * @author: oleg
 * @date: Jul 21, 2008
 */
public abstract class RubyInspectionVisitor extends RubyElementVisitor
{
	private ProblemsHolder myHolder;

	public RubyInspectionVisitor(@Nonnull final ProblemsHolder holder)
	{
		myHolder = holder;
	}

	protected final void registerProblem(@Nullable final PsiElement element, @Nonnull final String message)
	{
		if(element == null || element.getTextLength() == 0)
		{
			return;
		}
		myHolder.registerProblem(element, message);
	}

	protected final void registerProblem(@Nullable final PsiElement element, @Nonnull final String message, @Nonnull final LocalQuickFix quickFix)
	{
		if(element == null || element.getTextLength() == 0)
		{
			return;
		}
		myHolder.registerProblem(element, message, quickFix);
	}
}
