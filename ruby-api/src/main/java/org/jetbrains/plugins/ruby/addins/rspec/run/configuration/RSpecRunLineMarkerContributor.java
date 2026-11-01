package org.jetbrains.plugins.ruby.addins.rspec.run.configuration;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import org.jetbrains.plugins.ruby.addins.rspec.RSpecUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.iterators.RBlockCall;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.tests.RubyTestRunLineMarkerContributor;
import org.jspecify.annotations.Nullable;

/**
 * Run icons for the examples and example groups of spec files.
 */
@ExtensionImpl
public class RSpecRunLineMarkerContributor extends RubyTestRunLineMarkerContributor
{
	@RequiredReadAction
	@Nullable
	@Override
	public Info getInfo(PsiElement element)
	{
		if(element.getFirstChild() != null)
		{
			return null;
		}
		final PsiFile file = element.getContainingFile();
		if(!(file instanceof RFile) || !RSpecUtil.isRSpecTestFile(file.getVirtualFile()))
		{
			return null;
		}

		final RBlockCall blockCall = PsiTreeUtil.getParentOfType(element, RBlockCall.class);
		return blockCall != null && isFirstLeaf(element, blockCall) && RSpecUtil.isExampleOrGroup(blockCall) ? createInfo(blockCall) : null;
	}
}
