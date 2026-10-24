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

package org.jetbrains.plugins.ruby.ruby.run.confuguration.rubyScript;

import java.io.File;
import java.util.List;

import consulo.execution.configuration.LegacyRunConfigurationConverter;
import consulo.execution.configuration.ProgramParametersAware;
import consulo.execution.configuration.RunConfigurationOptions;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.configuration.fragment.ProgramParametersFragmentState;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.process.ExecutionException;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.AbstractRubyRunConfiguration;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyArgumentsFragment;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyLegacyRunConfigurationState;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyModuleFragment;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationUtil;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubySdkFragment;
import consulo.execution.configuration.ConfigurationFactory;

public class RubyRunConfiguration extends AbstractRubyRunConfiguration implements RubyRunConfigurationParams, ProgramParametersAware
{
	public static final String RUBY_RUN_CONFIG_SETTINGS_ID = "RUBY_RUN_CONFIG";

	private static final String SCRIPT_PATH = "SCRIPT_PATH";
	private static final String SCRIPT_ARGS = "SCRIPT_ARGS";

	public RubyRunConfiguration(final Project project, final ConfigurationFactory factory, final String name)
	{
		super(project, factory, name);
	}

	@Override
	protected Class<? extends RunConfigurationOptions> getOptionsClass()
	{
		return RubyRunConfigurationOptions.class;
	}

	@Override
	public RubyRunConfigurationOptions getOptions()
	{
		return (RubyRunConfigurationOptions) super.getOptions();
	}

	@Override
	protected void convertLegacyState(LegacyRunConfigurationConverter converter)
	{
		super.convertLegacyState(converter);

		RubyLegacyRunConfigurationState state = RubyLegacyRunConfigurationState.read(converter, getLegacySettingsId());
		RubyRunConfigurationOptions options = createLegacyOptions(state);
		convertLegacyCommonState(converter, state, options);
		options.setScriptPath(state.get(SCRIPT_PATH));
		converter.setOptions(options);

		converter.addFragment(new ProgramParametersFragmentState(state.get(SCRIPT_ARGS)));
	}

	/**
	 * The element name the old configuration wrote its options with.
	 */
	protected String getLegacySettingsId()
	{
		return RUBY_RUN_CONFIG_SETTINGS_ID;
	}

	/**
	 * Creates the options of a converted old configuration, filled with the settings specific to this configuration type.
	 */
	protected RubyRunConfigurationOptions createLegacyOptions(RubyLegacyRunConfigurationState state)
	{
		return new RubyRunConfigurationOptions();
	}

	@Override
	public List<RunConfigurationFragment<?>> createFragments()
	{
		return List.of(
				new RubyScriptFragment<>(),
				new RubyModuleFragment<>(),
				new RubySdkFragment<>(),
				new RubyArgumentsFragment<>()
		);
	}

	@Override
	public String getScriptPath()
	{
		return StringUtil.notNullize(getOptions().getScriptPath());
	}

	@Override
	public void setScriptPath(final String scriptPath)
	{
		getOptions().setScriptPath(scriptPath);
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


		return new RubyRunCommandLineState(this, executionEnvironment);
	}

	@Override
	protected void validateConfiguration(final boolean isExecution) throws Exception
	{
		RubyRunConfigurationUtil.inspectSDK(this, isExecution);

		// Script inspection
		if(getScriptPath() == null || getScriptPath().trim().length() == 0)
		{
			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.script.not.specified"), isExecution);
		}

		File script = new File(getScriptPath());
		if(!script.exists())
		{
			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.script.not.exists"), isExecution);
		}
		if(!script.isFile())
		{
			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.script.is.not.file"), isExecution);
		}

		RubyRunConfigurationUtil.inspectWorkingDirectory(this, isExecution);
	}

	@Override
	public String getScriptArgs()
	{
		return StringUtil.notNullize(getProgramParameters());
	}

	@Override
	public void setScriptArgs(String scriptArgs)
	{
		setProgramParameters(scriptArgs);
	}
}
