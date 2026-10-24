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

import consulo.application.util.SystemInfo;
import consulo.execution.configuration.LegacyRunConfigurationConverter;
import consulo.execution.configuration.RunConfigurationOptions;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.project.Project;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nullable;

import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.addins.rspec.RSpecModuleSettings;
import org.jetbrains.plugins.ruby.addins.rspec.RSpecUtil;
import org.jetbrains.plugins.ruby.jruby.JRubyUtil;
import org.jetbrains.plugins.ruby.rails.facet.RailsFacetUtil;
import org.jetbrains.plugins.ruby.ruby.RubyUtil;
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.AbstractRubyRunConfiguration;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyArgumentsFragment;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyLegacyRunConfigurationState;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyModuleFragment;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationUtil;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubySdkFragment;
import org.jetbrains.plugins.ruby.ruby.sdk.gemRootType.GemOrderRootType;
import consulo.process.ExecutionException;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.RunProfileState;
import consulo.module.Module;
import consulo.content.bundle.Sdk;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;

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
	private static final String SPEC_RUNNER_PATH = "SPEC_RUNNER_PATH";
	private static final String USE_COLOURED_OUTPUT_ENABLED = "USE_COLOURED_OUTPUT_ENABLED";
	private static final String RUN_SPECS_SEPARATELY = "RUN_SPECS_SEPARATELY";
	private static final String USE_CUSTOM_SPEC_RUNNER = "USE_CUSTOM_SPEC_RUNNER";

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
		options.setSpecArgs(state.get(SPEC_ARGS));
		options.setCustomSpecsRunnerPath(toCustomSpecsRunnerPath(state.get(SPEC_RUNNER_PATH)));
		options.setRunSpecsSeparately(state.getBoolean(RUN_SPECS_SEPARATELY));
		options.setUseColoredOutput(state.getBoolean(USE_COLOURED_OUTPUT_ENABLED));
		options.setUseCustomSpecRunner(state.getBoolean(USE_CUSTOM_SPEC_RUNNER));
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
				new RSpecArgumentsFragment(),
				new RSpecCustomRunnerFragment(),
				new RSpecColouredOutputFragment(),
				new RSpecRunSeparatelyFragment()
		);
	}

	@Nullable
	private static String toCustomSpecsRunnerPath(@Nullable String path)
	{
		return DEFAULT_CUSTOM_SPEC_RUNNER.equals(path) ? null : path;
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
				//testfolder
				inspectSpecsFolder(isExecution);
				break;
			case TEST_SCRIPT:
				//script
				inspectSpecScript(isExecution);
				break;
		}

		//if use custom runner
		if(shouldUseCustomSpecRunner())
		{
			inspectCustomRunnerScript(isExecution);
		}
		else
		{
			//If alternatie SDK is choosen we had to use gem
			inspectRSpecGemInAlternativeSDK(isExecution);

			//If module
			inspectRSpecInModule(isExecution);
		}

		final String args = getSpecArgs();
		if(shouldUseColoredOutput() || (args != null && args.contains(RSpecUtil.COLOURED_COMMAND_LINE_ARG)))
		{
			if(SystemInfo.isWindows)
			{
				final String[] gemsRootUrls = getSdk().getRootProvider().getUrls(GemOrderRootType.ID);
				for(String gemsRootUrl : gemsRootUrls)
				{
					final VirtualFile gemsRoot = VirtualFileManager.getInstance().findFileByUrl(gemsRootUrl);
					if(gemsRoot != null)
					{
						final VirtualFile[] files = gemsRoot.getChildren();
						for(VirtualFile file : files)
						{
							if(file.isDirectory() && file.getName().startsWith(RSpecUtil.WIN_32_CONSOLE_GEM))
							{
								final String msg = RBundle.message("rspec.run.configuration.test.plugin.doesnt.support.win32console.gem");
								RubyRunConfigurationUtil.throwExecutionOrRuntimeException(msg, isExecution);
							}
						}
					}
				}
			}
		}
	}

	private void inspectRSpecInModule(boolean isExecution) throws Exception
	{
		if(!shouldUseAlternativeSdk() && !shouldUseCustomSpecRunner())
		{
			final Module module = getModule();
			assert module != null;

			final Sdk sdk = getSdk();
			assert sdk != null;

			if(RailsFacetUtil.hasRailsSupport(module))
			{
				final boolean useRSpecPlugin = RSpecModuleSettings.getInstance(module).getRSpecSupportType() == RSpecModuleSettings.RSpecSupportType.RAILS_PLUGIN;
				if(useRSpecPlugin)
				{
					//chec plugin is installed
					final String railsAppHomeUrl = RailsFacetUtil.getRailsAppHomeDirPathUrl(module);
					assert railsAppHomeUrl != null;

					if(!RSpecUtil.isSpecScriptSupportInstalledInRailsProject(railsAppHomeUrl))
					{
						final String msg = RBundle.message("rspec.run.configuration.test.plugin.not.installed.install.or.change.to.gem");
						RubyRunConfigurationUtil.throwExecutionOrRuntimeException(msg, isExecution);
					}
					return;
				}
				assertRSpecGemIsInstalled(isExecution, sdk, false);
				return;
			}
			else if(RubyUtil.isRubyModuleType(module) || JRubyUtil.hasJRubySupport(module))
			{
				assertRSpecGemIsInstalled(isExecution, sdk, false);
				return;
			}
			final String msg = RBundle.message("rspec.run.configuration.test.module.should.be.ror");
			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(msg, isExecution);
		}
	}

	private void inspectRSpecGemInAlternativeSDK(boolean isExecution) throws Exception
	{
		if(shouldUseAlternativeSdk() && !shouldUseCustomSpecRunner())
		{
			final Sdk sdk = getAlternativeSdk();
			assert sdk != null;

			assertRSpecGemIsInstalled(isExecution, sdk, true);
		}
	}

	private void assertRSpecGemIsInstalled(boolean isExecution, Sdk sdk, final boolean isAlternativeSDK) throws Exception
	{
		if(!RSpecUtil.checkIfRSpecGemExists(sdk))
		{
			final String msg = isAlternativeSDK ? RBundle.message("rspec.run.configuration.test.no.gem.in.alternative.sdk") : RBundle.message("rspec.run.configuration.test.no.gem.in.module.sdk");

			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(msg, isExecution);
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

	private void inspectCustomRunnerScript(final boolean isExecution) throws Exception
	{
		final String scriptPath = getCustomSpecsRunnerPath().trim();
		if(TextUtil.isEmpty(scriptPath) || DEFAULT_CUSTOM_SPEC_RUNNER.equals(scriptPath))
		{
			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("rspec.run.configuration.test.custom.runner.not.exists"), isExecution);
		}

		final VirtualFile script = LocalFileSystem.getInstance().findFileByPath(scriptPath);
		if(script == null || !script.exists())
		{
			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("rspec.run.configuration.test.custom.runner.not.exists"), isExecution);
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
	public boolean shouldUseColoredOutput()
	{
		return getOptions().isUseColoredOutput();
	}

	@Override
	public void setShouldUseColoredOutput(final boolean enabled)
	{
		getOptions().setUseColoredOutput(enabled);
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
	public String getCustomSpecsRunnerPath()
	{
		return StringUtil.notNullize(getOptions().getCustomSpecsRunnerPath()).trim();
	}

	@Override
	public void setCustomSpecsRunnerPath(final String specsRunnerPath)
	{
		getOptions().setCustomSpecsRunnerPath(toCustomSpecsRunnerPath(specsRunnerPath));
	}

	@Override
	public void setShouldUseCustomSpecRunner(boolean useCustomSpecRunner)
	{
		getOptions().setUseCustomSpecRunner(useCustomSpecRunner);
	}

	@Override
	public boolean shouldUseCustomSpecRunner()
	{
		return getOptions().isUseCustomSpecRunner();
	}

	@Override
	public boolean shouldRunSpecSeparately()
	{
		return getOptions().isRunSpecsSeparately();
	}

	@Override
	public void setShouldRunSpecSeparately(final boolean shouldRunSpecSeparately)
	{
		getOptions().setRunSpecsSeparately(shouldRunSpecSeparately);
	}
}
