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

import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;

import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;

import java.io.File;
import java.util.List;

import jakarta.annotation.Nonnull;

import consulo.execution.configuration.LegacyRunConfigurationConverter;
import consulo.execution.configuration.RunConfigurationOptions;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.executor.Executor;
import consulo.project.Project;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.RubyUtil;
import org.jetbrains.plugins.ruby.ruby.cache.RCacheUtil;
import org.jetbrains.plugins.ruby.ruby.cache.psi.impl.RVirtualClassUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.Context;
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.presentation.RMethodPresentationUtil;
import org.jetbrains.plugins.ruby.ruby.presentation.RPresentationConstants;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.AbstractRubyRunConfiguration;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyArgumentsFragment;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyLegacyRunConfigurationState;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyModuleFragment;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationUtil;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubySdkFragment;
import consulo.process.ExecutionException;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.util.lang.ref.Ref;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.language.psi.scope.GlobalSearchScope;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 18.07.2007
 */
public class RTestsRunConfiguration extends AbstractRubyRunConfiguration implements RTestRunConfigurationParams
{
	public static final String DEFAULT_TESTS_SEARCH_MASK = "**/*." + RubyFileType.INSTANCE.getDefaultExtension();

	private static final RMethod[] EMPTY_VIRT_METHODS = new RMethod[0];

	public static final String RTEST_RUN_CONFIG_SETTINGS_ID = "RTEST_RUN_CONFIG_SETTINGS_ID";

	private static final String TEST_CLASS_NAME = "TEST_CLASS_NAME";
	private static final String TEST_METHOD_NAME = "TEST_METHOD_NAME";
	private static final String INHERITANCE_CHECK_DISABLED = "INHERITANCE_CHECK_DISABLED";

	public RTestsRunConfiguration(final Project project, final ConfigurationFactory factory, final String name)
	{
		super(project, factory, name);

		setRubyArgs(RubyRunConfigurationUtil.collectArguments(RubyUtil.RUN_IN_CONSOLE_HACK_ARGUMENTS));
	}

	@Override
	protected Class<? extends RunConfigurationOptions> getOptionsClass()
	{
		return RTestsRunConfigurationOptions.class;
	}

	@Override
	public RTestsRunConfigurationOptions getOptions()
	{
		return (RTestsRunConfigurationOptions) super.getOptions();
	}

	@Override
	protected void convertLegacyState(LegacyRunConfigurationConverter converter)
	{
		super.convertLegacyState(converter);

		RubyLegacyRunConfigurationState state = RubyLegacyRunConfigurationState.read(converter, RTEST_RUN_CONFIG_SETTINGS_ID);
		RTestsRunConfigurationOptions options = new RTestsRunConfigurationOptions();
		convertLegacyCommonState(converter, state, options);
		convertLegacyTestTargetState(state, options);
		options.setTestClassName(state.get(TEST_CLASS_NAME));
		options.setTestMethodName(state.get(TEST_METHOD_NAME));
		options.setInheritanceCheckDisabled(state.getBoolean(INHERITANCE_CHECK_DISABLED));
		converter.setOptions(options);
	}

	@Override
	public List<RunConfigurationFragment<?>> createFragments()
	{
		return List.of(
				new RTestsTargetFragment(),
				new RubyModuleFragment<>(),
				new RubySdkFragment<>(),
				new RubyArgumentsFragment<>(),
				new RTestsInheritanceCheckFragment()
		);
	}

	@Override
	public RunProfileState getState(@Nonnull Executor executor, @Nonnull ExecutionEnvironment executionEnvironment) throws ExecutionException
	{
		try
		{
			validateConfiguration(true);
		}
		catch(ExecutionException ee)
		{
			throw ee;
		}
		catch(Exception e)
		{
			throw new ExecutionException(e.getMessage(), e);
		}


		return new RTestsRunCommandLineState(this, executionEnvironment);
	}

	@Override
	protected void validateConfiguration(boolean isExecution) throws Exception
	{
		RubyRunConfigurationUtil.inspectSDK(this, isExecution);
		RubyRunConfigurationUtil.inspectWorkingDirectory(this, isExecution);

		switch(getTestType())
		{
			case ALL_IN_FOLDER:
				//testfolder
				inspectTestsFolder(isExecution);

				//mask
				if(TextUtil.isEmpty(getTestFileMask()))
				{
					RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.test.no.mask"), isExecution);
				}
				break;
			case TEST_CLASS:
				//class and script
				inspectTestClassAndScript(isExecution, null);
				break;
			case TEST_METHOD:
				//class and script
				final Ref<FileSymbol> fSWrapper = new Ref<FileSymbol>();
				inspectTestClassAndScript(isExecution, fSWrapper);

				//method
				inspectMethod(isExecution, fSWrapper);

				break;
			case TEST_SCRIPT:
				//script
				inspectTestScript(isExecution);
				break;
		}
	}

	private void inspectTestScript(final boolean isExecution) throws Exception
	{
		final String scriptPath = getTestScriptPath().trim();
		final VirtualFile script = LocalFileSystem.getInstance().findFileByPath(scriptPath);
		if(TextUtil.isEmpty(scriptPath) || script == null || !script.exists())
		{
			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.test.script.not.exists"), isExecution);
		}

		//noinspection ConstantConditions
		if(script.isDirectory())
		{
			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.test.script.is.dir"), isExecution);
		}
	}

	private void inspectMethod(final boolean isExecution, final Ref<FileSymbol> fSWrapper) throws Exception
	{

		final String methodName = getTestMethodName().trim();
		if(!TextUtil.isEmpty(methodName))
		{
			final String className = getTestQualifiedClassName().trim();
			final VirtualFile script = LocalFileSystem.getInstance().findFileByPath(getTestScriptPath());
			assert script != null;
			final RClass rClass = RCacheUtil.getClassByNameInScriptInRubyTestMode(className, getProject(), GlobalSearchScope.allScope(getProject()), script, fSWrapper);
			assert rClass != null;

			final Pair<Symbol, FileSymbol> pair = SymbolUtil.getSymbolByContainerRubyTestMode(rClass, fSWrapper);

			final RMethod[] methods;
			if(pair == null)
			{
				methods = EMPTY_VIRT_METHODS;
			}
			else
			{
				methods = RVirtualClassUtil.getAllMethodsWithName(pair.first, pair.second, methodName, Context.ALL);
			}

			for(RMethod method : methods)
			{
				final String name = RMethodPresentationUtil.formatName(method, RPresentationConstants.SHOW_NAME);
				if(methodName.equals(name))
				{
					return;
				}
			}
			if(isInheritanceCheckDisabled())
			{
				return; // we are not sure that all methods were found in test mode.
			}
		}
		RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.test.method.not.exists"), isExecution);
	}

	/**
	 * Checks that class exists in script, and is inherited from Unit::Test::Case
	 *
	 * @param isExecution Is currunt mode Execution mode
	 * @param fSWrapper   fSWrapper if null will be ignored. If wrapper contains
	 *                    not null value, this value will be used for comparing qualified names, otherwise method
	 *                    will store evaluated light mode symbol.
	 * @throws Exception If mode isExecution then Execution exception, otherwisee RuntimeException
	 */
	private void inspectTestClassAndScript(final boolean isExecution, final Ref<FileSymbol> fSWrapper) throws Exception
	{
		final String className = getTestQualifiedClassName().trim();
		if(!TextUtil.isEmpty(className))
		{
			final String scriptPath = getTestScriptPath().trim();
			// Empty scriptPath mean that class wasn't chosen
			if(!TextUtil.isEmpty(scriptPath))
			{
				//if some class is chosen then let's check script
				inspectTestScript(isExecution);

				final VirtualFile script = LocalFileSystem.getInstance().findFileByPath(scriptPath);
				assert script != null; //has been already checked
				final Project project = getProject();
				final GlobalSearchScope classSearchScope = GlobalSearchScope.allScope(project);

				final RClass rClass = RCacheUtil.getClassByNameInScriptInRubyTestMode(className, project, classSearchScope, script, fSWrapper);
				if(rClass != null)
				{
					if(isInheritanceCheckDisabled() || RTestUnitUtil.isClassUnitTestCase(rClass, fSWrapper))
					{
						return;
					}
					else
					{
						RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.test.class.must.inherited.from.testcase"), isExecution);
					}
				}
			}
		}
		RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.test.class.not.exists"), isExecution);
	}

	private void inspectTestsFolder(final boolean isExecution) throws Exception
	{
		final String folderPath = getTestsFolderPath().trim();
		File folder = new File(folderPath);
		if(!folder.exists())
		{
			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.test.folder.not.exists"), isExecution);
		}

		if(!folder.isDirectory())
		{
			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.test.folder.not.dir"), isExecution);
		}
	}

	@Override
	public String getTestsFolderPath()
	{
		return StringUtil.notNullize(getOptions().getTestsFolderPath());
	}

	@Override
	public String getTestScriptPath()
	{
		return StringUtil.notNullize(getOptions().getTestScriptPath());
	}

	@Override
	public String getTestMethodName()
	{
		return StringUtil.notNullize(getOptions().getTestMethodName());
	}

	@Override
	public TestType getTestType()
	{
		return parseTestType(getOptions().getTestType());
	}

	@Override
	public String getTestQualifiedClassName()
	{
		return StringUtil.notNullize(getOptions().getTestClassName());
	}

	@Override
	public String getTestFileMask()
	{
		return StringUtil.notNullize(getOptions().getTestFileMask());
	}

	/**
	 * Path to folder
	 *
	 * @param path Path should contains only ruby style path separator: "/"
	 */
	@Override
	public void setTestsFolderPath(final String path)
	{
		getOptions().setTestsFolderPath(path);
	}

	@Override
	public void setTestScriptPath(final String pathOrMask)
	{
		getOptions().setTestScriptPath(pathOrMask);
	}

	@Override
	public void setTestMethodName(@Nullable final String name)
	{
		getOptions().setTestMethodName(name);
	}

	@Override
	public void setTestType(@Nonnull final TestType testType)
	{
		getOptions().setTestType(testType.name());
	}

	@Override
	public void setTestQualifiedClassName(@Nullable final String testClassName)
	{
		getOptions().setTestClassName(testClassName);
	}

	@Override
	public void setTestFileMask(final String testFileMask)
	{
		getOptions().setTestFileMask(testFileMask);
	}

	@Override
	public boolean isInheritanceCheckDisabled()
	{
		return getOptions().isInheritanceCheckDisabled();
	}

	@Override
	public void setInheritanceCheckDisabled(final boolean disabled)
	{
		getOptions().setInheritanceCheckDisabled(disabled);
	}
}
