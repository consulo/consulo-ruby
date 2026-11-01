package org.jetbrains.plugins.ruby.ruby.run.confuguration.tests;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.names.RClassName;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.names.RMethodName;
import org.jetbrains.plugins.ruby.ruby.lang.psi.iterators.RBlockCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import org.jspecify.annotations.Nullable;

/**
 * Run icons for test case classes, their test methods and declarative <code>test "..."</code> blocks.
 */
@ExtensionImpl
public class RTestRunLineMarkerContributor extends RubyTestRunLineMarkerContributor
{
	@RequiredReadAction
	@Nullable
	@Override
	public Info getInfo(PsiElement element)
	{
		if(element.getFirstChild() != null || !(element.getContainingFile() instanceof RFile))
		{
			return null;
		}

		final RClassName className = PsiTreeUtil.getParentOfType(element, RClassName.class);
		if(className != null && isFirstLeaf(element, className) && className.getParent() instanceof RClass rClass)
		{
			return RTestUnitUtil.isClassUnitTestCase(rClass, null) ? createInfo(rClass) : null;
		}

		final RMethodName methodName = PsiTreeUtil.getParentOfType(element, RMethodName.class);
		if(methodName != null && isFirstLeaf(element, methodName) && methodName.getParent() instanceof RMethod method)
		{
			final RClass rClass = method.getParentContainer() instanceof RClass container ? container : null;
			if(rClass != null && RTestUnitUtil.hasValidTestNameAndNotSingleton(method) && RTestUnitUtil.isClassUnitTestCase(rClass, null))
			{
				return createInfo(method);
			}
			return null;
		}

		final RCall call = PsiTreeUtil.getParentOfType(element, RCall.class);
		if(call != null && call.getParent() instanceof RBlockCall blockCall && blockCall.getCall() == call && isFirstLeaf(element, call.getPsiCommand()))
		{
			final RClass rClass = PsiTreeUtil.getParentOfType(blockCall, RClass.class);
			if(rClass != null && RTestUnitUtil.isDeclarativeTest(blockCall) && RTestUnitUtil.isClassUnitTestCase(rClass, null))
			{
				return createInfo(blockCall);
			}
		}
		return null;
	}
}
