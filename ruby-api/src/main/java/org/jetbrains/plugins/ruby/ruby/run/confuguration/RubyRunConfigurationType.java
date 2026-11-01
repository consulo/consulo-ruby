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

package org.jetbrains.plugins.ruby.ruby.run.confuguration;

import static org.jetbrains.plugins.ruby.ruby.run.confuguration.AbstractRubyRunConfiguration.TestType;

import consulo.annotation.component.ExtensionImpl;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import consulo.execution.action.Location;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.ConfigurationTypeUtil;
import consulo.localize.LocalizeValue;
import consulo.language.psi.PsiFile;
import consulo.navigation.OpenFileDescriptor;
import consulo.util.lang.ref.Ref;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.addins.rspec.RSpecUtil;
import org.jetbrains.plugins.ruby.rails.facet.RailsFacetUtil;
import org.jetbrains.plugins.ruby.ruby.RubyIcons;
import org.jetbrains.plugins.ruby.ruby.RubyUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;
import org.jetbrains.plugins.ruby.ruby.presentation.RClassPresentationUtil;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.rubyScript.RubyRunConfiguration;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.rubyScript.RubyRunConfigurationFactory;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.tests.RTestUnitUtil;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.tests.RTestsRunConfiguration;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.tests.RTestsRunConfigurationFactory;
import org.jetbrains.plugins.ruby.support.utils.RubyVirtualFileScanner;
import consulo.execution.configuration.ConfigurationType;
import consulo.execution.configuration.RunConfiguration;
import consulo.module.Module;
import consulo.project.Project;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.util.PsiTreeUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.iterators.RBlockCall;
import java.util.Objects;
import consulo.module.content.ProjectRootManager;
import consulo.language.psi.PsiElement;
import consulo.ui.image.Image;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 20.07.2006
 */
@ExtensionImpl
public class RubyRunConfigurationType implements ConfigurationType
{
	private final RubyRunConfigurationFactory myAppFactory;
	private final RTestsRunConfigurationFactory myUnitTestsFactory;

	public RubyRunConfigurationType()
	{
		myAppFactory = new RubyRunConfigurationFactory(this);
		myUnitTestsFactory = new RTestsRunConfigurationFactory(this);
	}

	public static RubyRunConfigurationType getInstance()
	{
		return ConfigurationTypeUtil.findConfigurationType(RubyRunConfigurationType.class);
	}

	@Nonnull
	@Override
	public LocalizeValue getDisplayName()
	{
		return LocalizeValue.localizeTODO(RBundle.message("run.configuration.type.name"));
	}

	@Nonnull
	@Override
	public LocalizeValue getConfigurationTypeDescription()
	{
		return LocalizeValue.localizeTODO(RBundle.message("run.configuration.type.description"));
	}

	@Override
	public Image getIcon()
	{
		return RubyIcons.RUBY_RUN_CONFIGURATION_FOLDER;
	}

	@Nonnull
	@Override
	public String getId()
	{
		return "RubyRunConfigurationType";
	}

	@Override
	public ConfigurationFactory[] getConfigurationFactories()
	{
		return new ConfigurationFactory[]{
				myAppFactory,
				myUnitTestsFactory
		};
	}

	/**
	 * Finds what a run started at the location runs: a script, a test file, class, method or folder.
	 */
	@Nullable
	public RubyRunTarget findTarget(final @Nonnull Location location)
	{
		final PsiElement locationElement = location.getPsiElement();
		final RFile containingFile = RubyPsiUtil.getRFile(locationElement);
		final OpenFileDescriptor openedFile = location.getOpenFileDescriptor();

		if(containingFile == null || openedFile == null)
		{
			if(locationElement instanceof PsiDirectory directory && RTestUnitUtil.containsTestFiles(directory.getVirtualFile()))
			{
				final VirtualFile folder = directory.getVirtualFile();
				return new RubyRunTarget(FileSemantic.TEST_UNIT_TEST, folder, locationElement, RBundle.message("run.configuration.test.default.name", folder.getName()), TestType.ALL_IN_FOLDER, null, null);
			}
			return null;
		}

		final VirtualFile file = openedFile.getFile();
		// a Rakefile declares tasks, running it as a script does nothing
		if(!RubyVirtualFileScanner.isRubyFile(file) || RSpecUtil.isRSpecTestFile(file) || isRakeFile(file))
		{
			return null;
		}

		final RPsiElement rPsiElement = RubyPsiUtil.getCoveringRPsiElement(locationElement);
		final RContainer currentContainer = rPsiElement == null ? null : rPsiElement instanceof RContainer container ? container : rPsiElement.getParentContainer();
		final String fileName = file.getNameWithoutExtension();

		if(currentContainer instanceof RClass || currentContainer instanceof RMethod)
		{
			// a nested class runs with the class around it
			final RClass upperClass = RubyPsiUtil.getContainingUpperRClass(currentContainer);
			final Ref<FileSymbol> fSWrapper = new Ref<FileSymbol>();
			if(upperClass == null || !RTestUnitUtil.isClassUnitTestCase(upperClass, fSWrapper))
			{
				return new RubyRunTarget(FileSemantic.RUBY_SCRIPT, file, containingFile, fileName, null, null, null);
			}

			final String className = RClassPresentationUtil.getRuntimeQualifiedNameInRubyTestMode(upperClass, fSWrapper);
			if(currentContainer instanceof RMethod method && upperClass == method.getParentContainer() && RTestUnitUtil.hasValidTestNameAndNotSingleton(method))
			{
				return new RubyRunTarget(FileSemantic.TEST_UNIT_TEST, file, method, method.getName(), TestType.TEST_METHOD, className, method.getName());
			}

			if(currentContainer instanceof RClass)
			{
				final RBlockCall blockCall = PsiTreeUtil.getParentOfType(locationElement, RBlockCall.class, false, RClass.class);
				final String methodName = RTestUnitUtil.getDeclarativeTestMethodName(locationElement, upperClass);
				if(methodName != null && blockCall != null)
				{
					return new RubyRunTarget(FileSemantic.TEST_UNIT_TEST, file, blockCall, className + "." + methodName, TestType.TEST_METHOD, className, methodName);
				}
			}

			final TestType testType = calculateTestTypeForClass(upperClass);
			if(testType == null)
			{
				return null;
			}
			return new RubyRunTarget(FileSemantic.TEST_UNIT_TEST, file, upperClass, testType == TestType.TEST_SCRIPT ? fileName : className, testType, className, null);
		}

		final boolean tests = currentContainer instanceof RFile rFile && RTestUnitUtil.checkForAnotherTestCases(null, rFile);
		return tests
			? new RubyRunTarget(FileSemantic.TEST_UNIT_TEST, file, containingFile, fileName, TestType.TEST_SCRIPT, null, null)
			: new RubyRunTarget(FileSemantic.RUBY_SCRIPT, file, containingFile, fileName, null, null, null);
	}

	private static boolean isRakeFile(final VirtualFile file)
	{
		return "Rakefile".equals(file.getName()) || "rakefile".equals(file.getName()) || "rake".equals(file.getExtension());
	}

	/**
	 * Fills a configuration of the target's kind, keeping the defaults of its template.
	 *
	 * @return false if no module of the project contains the target
	 */
	public boolean setupConfiguration(final @Nonnull AbstractRubyRunConfiguration configuration, final @Nonnull RubyRunTarget target)
	{
		final Project project = configuration.getProject();
		final VirtualFile file = target.getFile();
		final Module module = RubyRunConfigurationUtil.getCorrespondingModule(project, file, configuration);
		if(module == null)
		{
			return false;
		}
		configuration.setName(target.getName());
		configuration.setModule(module);

		if(TextUtil.isEmptyOrWhitespaces(configuration.getWorkingDirectory()))
		{
			// tests run in the project root, where Rails finds its configuration and fixtures
			final VirtualFile contentRoot = ProjectRootManager.getInstance(project).getFileIndex().getContentRootForFile(file);
			final VirtualFile directory = target.getSemantic() == FileSemantic.TEST_UNIT_TEST && contentRoot != null ? contentRoot : file.isDirectory() ? file : file.getParent();
			configuration.setWorkingDirectory(directory != null ? directory.getPath() : RailsFacetUtil.getRailsAppHomeDirPath(module));
		}

		if(target.getSemantic() == FileSemantic.RUBY_SCRIPT)
		{
			((RubyRunConfiguration) configuration).setScriptPath(file.getPath());
		}
		else if(target.getTestType() == TestType.ALL_IN_FOLDER)
		{
			final RTestsRunConfiguration conf = (RTestsRunConfiguration) configuration;
			conf.setTestType(TestType.ALL_IN_FOLDER);
			conf.setTestsFolderPath(file.getPath());
			if(TextUtil.isEmptyOrWhitespaces(conf.getTestFileMask()))
			{
				conf.setTestFileMask(RTestsRunConfiguration.DEFAULT_TESTS_SEARCH_MASK);
			}
		}
		else
		{
			initUnitTestConfiguration(file, target.getTestType(), target.getClassName(), target.getMethodName(), configuration);
		}
		return true;
	}

	/**
	 * @return true if the configuration runs the target
	 */
	public boolean isConfigurationOfTarget(final @Nonnull RunConfiguration configuration, final @Nonnull RubyRunTarget target)
	{
		final String path = target.getFile().getPath();
		if(target.getSemantic() == FileSemantic.RUBY_SCRIPT)
		{
			return configuration instanceof RubyRunConfiguration script && path.equals(script.getScriptPath());
		}
		if(!(configuration instanceof RTestsRunConfiguration conf) || conf.getTestType() != target.getTestType())
		{
			return false;
		}
		return switch(target.getTestType())
		{
			case ALL_IN_FOLDER -> path.equals(conf.getTestsFolderPath());
			case TEST_SCRIPT -> path.equals(conf.getTestScriptPath());
			case TEST_CLASS -> path.equals(conf.getTestScriptPath()) && Objects.equals(target.getClassName(), conf.getTestQualifiedClassName());
			case TEST_METHOD -> path.equals(conf.getTestScriptPath()) && Objects.equals(target.getClassName(), conf.getTestQualifiedClassName()) && Objects.equals(target.getMethodName(), conf.getTestMethodName());
		};
	}

	public RubyRunConfigurationFactory getScriptFactory()
	{
		return myAppFactory;
	}

	public RTestsRunConfigurationFactory getTestsFactory()
	{
		return myUnitTestsFactory;
	}


	/**
	 * If our Unit TestCase is single testcase in script, we can run whole script
	 *
	 * @param rClass Given ruby class
	 * @return TestType.TEST_CLASS or TestType.TEST_SCRIPT
	 */
	private TestType calculateTestTypeForClass(final RClass rClass)
	{
		final TestType testType;
		final PsiFile psiFile = rClass.getContainingFile();
		if(psiFile instanceof RFile)
		{
			testType = RTestUnitUtil.checkForAnotherTestCases(rClass, (RFile) psiFile) ? AbstractRubyRunConfiguration.TestType.TEST_CLASS : AbstractRubyRunConfiguration.TestType.TEST_SCRIPT;
		}
		else
		{
			testType = null;
		}
		return testType;
	}

	private void initUnitTestConfiguration(final VirtualFile file, final TestType testType, @Nullable final String className, @Nullable final String methodName, final AbstractRubyRunConfiguration configuration)
	{
		final RTestsRunConfiguration conf = (RTestsRunConfiguration) configuration;

		//ignores template configuration's defaults
		assert testType != null;
		conf.setTestType(testType);
		conf.setTestScriptPath(file.getPath());

		//keeps template configuration's defaults
		// with shift (by default for all)
		RubyRunConfigurationUtil.setupRubyArgs(conf, RubyRunConfigurationUtil.collectArguments(RubyUtil.RUN_IN_CONSOLE_HACK_ARGUMENTS));

		//special settings
		switch(testType)
		{
			case TEST_CLASS:
				//ignores template configuration's defaults
				conf.setTestQualifiedClassName(className);
				//keeps template configuration's defaults
				// without shift, special for test class
				RubyRunConfigurationUtil.setupRubyArgs(conf, RubyRunConfigurationUtil.collectArguments(RubyUtil.RUN_IN_CONSOLE_HACK_ARGUMENTS_NO_SHIFT));
				break;
			case TEST_METHOD:
				//ignores template configuration's defaults
				conf.setTestQualifiedClassName(className);
				conf.setTestMethodName(methodName);
				break;
			case TEST_SCRIPT:
				//Do nothing
				break;
		}
	}

	public enum FileSemantic
	{
		RSPEC_TEST,
		TEST_UNIT_TEST,
		RUBY_SCRIPT,
	}
}
