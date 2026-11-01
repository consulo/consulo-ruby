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

package org.jetbrains.plugins.ruby.addins.rspec.run.configuration;

import java.io.File;
import java.util.List;

import jakarta.annotation.Nonnull;

import consulo.execution.configuration.LegacyRunConfigurationConverter;
import consulo.execution.configuration.RunConfigurationOptions;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.project.Project;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nullable;

import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.RubyUtil;
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.AbstractRubyRunConfiguration;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyArgumentsFragment;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyLegacyRunConfigurationState;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyModuleFragment;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationUtil;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubySdkFragment;
import consulo.process.ExecutionException;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.RunProfileState;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: Oct 18, 2007
 */
public class RSpecRunConfiguration extends AbstractRubyRunConfiguration implements RSpecRunConfigurationParams
{
	public static final String DEFAULT_TESTS_SEARCH_MASK = "**/*_spec." + RubyFileType.INSTANCE.getDefaultExtension();
	public static final String DEFAULT_SPEC_ARGS = "-fs";

	public static String DEFAULT_CUSTOM_SPEC_RUNNER = RBundle.message("run.configuration.messages.none");

	public static final String RSPEC_RUN_CONFIG_SETTINGS_ID = "RSPEC_RUN_CONFIG_SETTINGS_ID";

	private static final String SPEC_ARGS = "SPEC_ARGS";

	public RSpecRunConfiguration(final Project project, final ConfigurationFactory factory, final String name)
	{
		super(project, factory, name);

		setRubyArgs(RubyRunConfigurationUtil.collectArguments(RubyUtil.RUN_IN_CONSOLE_HACK_ARGUMENTS));
	}

	@Override
	protected Class<? extends RunConfigurationOptions> getOptionsClass()
	{
		return RSpecRunConfigurationOptions.class;
	}

	@Override
	public RSpecRunConfigurationOptions getOptions()
	{
		return (RSpecRunConfigurationOptions) super.getOptions();
	}

	@Override
	protected void convertLegacyState(LegacyRunConfigurationConverter converter)
	{
		super.convertLegacyState(converter);

		RubyLegacyRunConfigurationState state = RubyLegacyRunConfigurationState.read(converter, RSPEC_RUN_CONFIG_SETTINGS_ID);
		RSpecRunConfigurationOptions options = new RSpecRunConfigurationOptions();
		convertLegacyCommonState(converter, state, options);
		convertLegacyTestTargetState(state, options);
		options.setSpecArgs(withoutRSpec1Format(state.get(SPEC_ARGS)));
		converter.setOptions(options);
	}

	@Override
	public List<RunConfigurationFragment<?>> createFragments()
	{
		return List.of(
				new RSpecTargetFragment(),
				new RubyModuleFragment<>(),
				new RubySdkFragment<>(),
				new RubyArgumentsFragment<>(),
				new RSpecArgumentsFragment()
		);
	}

	/**
	 * The specdoc format of RSpec 1 was the default arguments of old configurations
	 */
	@Nullable
	private static String withoutRSpec1Format(@Nullable String args)
	{
		return args == null ? null : args.replace("-fs", "").trim();
	}

	@Nullable
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


		return new RSpecRunCommandLineState(this, executionEnvironment);

	}


	@Override
	protected void validateConfiguration(boolean isExecution) throws Exception
	{
		RubyRunConfigurationUtil.inspectSDK(this, isExecution);
		RubyRunConfigurationUtil.inspectWorkingDirectory(this, isExecution);

		switch(getTestType())
		{
			case ALL_IN_FOLDER:
				inspectSpecsFolder(isExecution);
				break;
			case TEST_SCRIPT:
				inspectSpecScript(isExecution);
				break;
			case TEST_METHOD:
				inspectSpecScript(isExecution);
				if(getExampleLine() <= 0)
				{
					RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("rspec.run.configuration.test.line.not.specified"), isExecution);
				}
				break;
			default:
				break;
		}
	}

	private void inspectSpecScript(final boolean isExecution) throws Exception
	{
		final String scriptPath = getTestScriptPath().trim();
		final VirtualFile script = LocalFileSystem.getInstance().findFileByPath(scriptPath);
		if(TextUtil.isEmpty(scriptPath) || script == null || !script.exists())
		{
			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("rspec.run.configuration.test.script.not.exists"), isExecution);
		}

		//noinspection ConstantConditions
		if(script.isDirectory())
		{
			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("rspec.run.configuration.test.script.is.dir"), isExecution);
		}
	}

	private void inspectSpecsFolder(final boolean isExecution) throws Exception
	{
		final String folderPath = getTestsFolderPath().trim();
		File folder = new File(folderPath);
		if(!folder.exists())
		{
			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("rspec.run.configuration.test.folder.not.exists"), isExecution);
		}

		if(!folder.isDirectory())
		{
			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("rspec.run.configuration.test.folder.not.dir"), isExecution);
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
	public TestType getTestType()
	{
		return parseTestType(getOptions().getTestType());
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
	public void setTestType(@Nonnull final TestType testType)
	{
		getOptions().setTestType(testType.name());
	}

	@Override
	public void setTestFileMask(final String testFileMask)
	{
		getOptions().setTestFileMask(testFileMask);
	}

	@Override
	public String getSpecArgs()
	{
		return StringUtil.notNullize(getOptions().getSpecArgs());
	}

	@Override
	public void setSpecArgs(final String specArgs)
	{
		getOptions().setSpecArgs(specArgs);
	}

	@Override
	public int getExampleLine()
	{
		return getOptions().getExampleLine();
	}

	@Override
	public void setExampleLine(final int exampleLine)
	{
		getOptions().setExampleLine(exampleLine);
	}
}
