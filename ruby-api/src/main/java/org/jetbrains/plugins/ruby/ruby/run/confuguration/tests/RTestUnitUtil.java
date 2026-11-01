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

package org.jetbrains.plugins.ruby.ruby.run.confuguration.tests;

import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RSingletonMethod;

import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;

import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import consulo.util.lang.Pair;
import org.jetbrains.plugins.ruby.rails.RailsConstants;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RVirtualName;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.data.Children;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.names.RSuperClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.holders.utils.RContainerUtil;
import org.jetbrains.plugins.ruby.ruby.roots.RubyModuleRootUtil;
import consulo.language.psi.PsiElement;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.Comparing;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.basicTypes.stringLiterals.RStringLiteral;
import org.jetbrains.plugins.ruby.ruby.lang.psi.iterators.RBlockCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import consulo.util.lang.ref.Ref;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 06.08.2007
 */
public class RTestUnitUtil
{
	public static final String TEST_METHOD_NAME_PREFIX = "test";

	/**
	 * The test case classes of Test::Unit, minitest and Rails
	 */
	public static final java.util.Set<String> TEST_CASE_BASE_CLASSES = java.util.Set.of(
			RailsConstants.TEST_UNIT_TESTCASE,
			"Minitest::Test", "MiniTest::Test", "MiniTest::Unit::TestCase",
			"ActiveSupport::TestCase", "ActionDispatch::IntegrationTest", "ActionDispatch::SystemTestCase",
			"ActionController::TestCase", "ActionMailer::TestCase", "ActionView::TestCase", "ActiveJob::TestCase",
			"ActionCable::TestCase", "ActionCable::Channel::TestCase", "ApplicationSystemTestCase");

	private static String getFullName(@Nonnull final Symbol symbol)
	{
		final StringBuilder name = new StringBuilder(String.valueOf(symbol.getName()));
		Symbol parent = symbol.getParentSymbol();
		while(parent != null && parent.getName() != null)
		{
			name.insert(0, parent.getName() + "::");
			parent = parent.getParentSymbol();
		}
		return name.toString();
	}

	/**
	 * Recursively checks if user class inherits Test::Unit::TestCase class
	 *
	 * @param rVClassSymbol Symbol for user class
	 * @param fileSymbol    FileSymbol
	 * @return True if inherits.
	 */
	public static boolean isUserUnitTestCase(@Nonnull final Symbol rVClassSymbol, @Nonnull final FileSymbol fileSymbol)
	{
		if(isBaseUnitTestCase(rVClassSymbol))
		{
			return true;
		}

		//Some times super class may have no virtual prototypes
		//e.g. class A < Test::Unit::TestCase;end; whrere
		// we ignore "test/unit" require.
		final Children superClassSymbols = rVClassSymbol.getChildren(fileSymbol).getSymbolsOfTypes(Type.SUPERCLASS.asSet());
		for(Symbol superClassSymbol : superClassSymbols.getAll())
		{
			if(isUserUnitTestCase(superClassSymbol.getLinkedSymbol(), fileSymbol))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if class is unit testcase class (Test::Unit::TestCase)
	 *
	 * @param rVClassSymbol Class
	 * @return True if class is Test::Unit::TestCase.
	 */

	public static boolean isBaseUnitTestCase(@Nonnull final Symbol rVClassSymbol)
	{
		if(TEST_CASE_BASE_CLASSES.contains(getFullName(rVClassSymbol)))
		{
			return true;
		}
		final String name = rVClassSymbol.getName();
		if(Comparing.equal(RailsConstants.TEST_CASE_CLASS_NAME, name))
		{
			final Symbol unitModuleSymbol = rVClassSymbol.getParentSymbol();
			if(unitModuleSymbol != null && Comparing.equal(RailsConstants.UNIT_MODULE_NAME, unitModuleSymbol.getName()))
			{

				final Symbol testUnitModuleSymb = unitModuleSymbol.getParentSymbol();
				return testUnitModuleSymb != null && Comparing.equal(RailsConstants.TEST_MODULE_NAME, testUnitModuleSymb.getName());
			}
		}
		return false;
	}

	/**
	 * Checks if class inherits form Test::Unit::TestCase using light test mode symbols.
	 * If ruby class isn't direct descendant of Test::Unit::TestCase, all other
	 * checks will be made in context of TEST folders of current project(speed optimization).
	 *
	 * @param rClass            Virtual(cache) or Psi Ruby class
	 * @param fileSymbolWrapper if null nothing will happen. If wrapper contains
	 *                          not null value, this value will be used for check, otherwise method
	 *                          will store evaluated light mode symbol.
	 * @return true if class is valid testcase class
	 */
	public static boolean isClassUnitTestCase(@Nonnull final RClass rClass, @Nullable final Ref<FileSymbol> fileSymbolWrapper)
	{
		// 1. lets check direct ancestor without symbol cashe
		final String fullName;
		if(rClass instanceof RClass)
		{
			//TODO it's awfull style...
			final RSuperClass rSuperClass = ((RClass) rClass).getPsiSuperClass();
			fullName = rSuperClass == null ? null : rSuperClass.getText();
		}
		else
		{
			final RVirtualName vSuperClass = rClass.getVirtualSuperClass();
			fullName = vSuperClass == null ? null : vSuperClass.getFullName();
		}
		if(fullName != null && TEST_CASE_BASE_CLASSES.contains(fullName.startsWith("::") ? fullName.substring(2) : fullName))
		{
			return true;
		}

		// 2. we consider only classes form TEST folder (optimization)
		final String url = rClass.getContainingFileUrl();
		if(!RubyModuleRootUtil.isUnderTestUnitRoot(rClass.getProject(), url))
		{
			return false;
		}

		// 3. check ancestors in light test mode
		final Pair<Symbol, FileSymbol> pair = SymbolUtil.getSymbolByContainerRubyTestMode(rClass, fileSymbolWrapper);
		return pair != null && pair.first != null && isUserUnitTestCase(pair.first, pair.second);
	}

	/**
	 * Checks if method name starts with prefix "test"
	 *
	 * @param method Some ruby method
	 * @return is method test method
	 */
	public static boolean hasValidTestNameAndNotSingleton(@Nonnull final RMethod method)
	{
		final String methodName = method.getName();
		return methodName.startsWith(TEST_METHOD_NAME_PREFIX) && methodName.length() > TEST_METHOD_NAME_PREFIX.length() && !(method instanceof RSingletonMethod);
	}

	/**
	 * Check if another unit test cases exeis in file.
	 *
	 * @param currentContainer RClass, given unit test case.
	 * @param rFile            Ruby File
	 * @return True if exist.
	 */
	public static boolean checkForAnotherTestCases(@Nullable final RContainer currentContainer, @Nonnull final RFile rFile)
	{
		final List<RClass> classes = RContainerUtil.getTopLevelClasses(rFile);
		final Ref<FileSymbol> fileSymbolWrapper = new Ref<FileSymbol>();

		for(RClass vClass : classes)
		{
			// Really all classes are "psi", not "virtual".
			if(currentContainer == vClass || !(vClass instanceof RClass))
			{
				continue;
			}
			// if exist another test case in script
			if(isClassUnitTestCase(vClass, fileSymbolWrapper))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Finds the declarative test (<code>test "does something" do ... end</code>) around the element.
	 *
	 * @param element element inside the test
	 * @param rClass  test case class containing the element
	 * @return name of the method the declaration defines, <code>test_does_something</code>
	 */
	@Nullable
	public static String getDeclarativeTestMethodName(@Nonnull final PsiElement element, @Nonnull final RClass rClass)
	{
		RBlockCall blockCall = PsiTreeUtil.getParentOfType(element, RBlockCall.class, false, RClass.class);
		while(blockCall != null)
		{
			final String name = getDeclarativeTestName(blockCall);
			if(name != null)
			{
				return toDeclarativeTestMethodName(name);
			}
			blockCall = PsiTreeUtil.getParentOfType(blockCall, RBlockCall.class, true, RClass.class);
		}
		return null;
	}

	/**
	 * @param rClass     test case class
	 * @param methodName name of the method a declarative test defines
	 * @return true if the class declares <code>test "..."</code> with this method name
	 */
	public static boolean hasDeclarativeTest(@Nonnull final RClass rClass, @Nonnull final String methodName)
	{
		for(RBlockCall blockCall : PsiTreeUtil.findChildrenOfType(rClass, RBlockCall.class))
		{
			final String name = getDeclarativeTestName(blockCall);
			if(name != null && methodName.equals(toDeclarativeTestMethodName(name)))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @return true if the folder or one of its subfolders holds a test file: <code>*_test.rb</code> or <code>test_*.rb</code>
	 */
	public static boolean containsTestFiles(@Nonnull final VirtualFile folder)
	{
		return !VirtualFileUtil.iterateChildrenRecursively(folder, null, file -> !isTestFile(file));
	}

	private static boolean isTestFile(@Nonnull final VirtualFile file)
	{
		final String name = file.getName();
		return !file.isDirectory() && name.endsWith(".rb") && (name.endsWith("_test.rb") || name.startsWith("test_"));
	}

	/**
	 * @return true for <code>test "does something" do ... end</code>
	 */
	public static boolean isDeclarativeTest(@Nonnull final RBlockCall blockCall)
	{
		return getDeclarativeTestName(blockCall) != null;
	}

	@Nonnull
	private static String toDeclarativeTestMethodName(@Nonnull final String name)
	{
		return TEST_METHOD_NAME_PREFIX + "_" + name.replaceAll("\\s+", "_");
	}

	@Nullable
	private static String getDeclarativeTestName(@Nonnull final RBlockCall blockCall)
	{
		if(!(blockCall.getCall() instanceof RCall call) || !TEST_METHOD_NAME_PREFIX.equals(call.getCommand()))
		{
			return null;
		}
		final List<RPsiElement> arguments = call.getArguments();
		if(arguments.size() != 1 || !(arguments.get(0) instanceof RStringLiteral literal) || literal.getText().contains("#{"))
		{
			return null;
		}
		return literal.getContent();
	}
}
