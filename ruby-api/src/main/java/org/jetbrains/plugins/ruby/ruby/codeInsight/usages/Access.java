package org.jetbrains.plugins.ruby.ruby.codeInsight.usages;

import javax.annotation.Nonnull;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;

/**
 * @author: oleg
 * @date: 06.05.2007
 */
public interface Access
{
	@Nonnull
	RPsiElement getElement();
}
