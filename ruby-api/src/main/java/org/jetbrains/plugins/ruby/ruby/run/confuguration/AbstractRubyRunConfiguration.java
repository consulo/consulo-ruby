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

import java.util.Map;

import jakarta.annotation.Nonnull;

import consulo.execution.RuntimeConfigurationException;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.EnvironmentAware;
import consulo.execution.configuration.LegacyRunConfigurationConverter;
import consulo.execution.configuration.LocatableConfiguration;
import consulo.execution.configuration.WorkingDirectoryAware;
import consulo.execution.configuration.fragment.EnvironmentFragmentState;
import consulo.execution.configuration.fragment.WorkingDirectoryFragmentState;
import consulo.module.ModuleManager;
import consulo.project.Project;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.tests.AbstractRTestsRunConfigurationOptions;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil;
import org.jetbrains.plugins.ruby.support.utils.RModuleUtil;
import consulo.execution.configuration.RunConfigurationBase;
import consulo.module.Module;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 18.07.2007
 */
public abstract class AbstractRubyRunConfiguration extends RunConfigurationBase implements LocatableConfiguration, AbstractRubyRunConfigurationParams, WorkingDirectoryAware,
		EnvironmentAware
{
	protected final static String SUGGESTED_NAME = RBundle.message("run.configuration.messages.suggested.name");

	private static final String RUBY_ARGS = "RUBY_ARGS";
	private static final String WORK_DIR = "WORK DIR";
	private static final String MODULE_NAME = "MODULE_NAME";
	private static final String SHOULD_USE_SDK = "SHOULD_USE_SDK";
	private static final String ALTERN_SDK_NAME = "ALTERN_SDK_NAME";
	private static final String PASS_PARENT_ENVS = "myPassParentEnvs";
	private static final String TESTS_FOLDER_PATH = "TESTS_FOLDER_PATH";
	private static final String TEST_SCRIPT_PATH = "TEST_SCRIPT_PATH";
	private static final String TEST_FILE_MASK = "TEST_FILE_MASK";
	private static final String TEST_TEST_TYPE = "TEST_TEST_TYPE";

	protected AbstractRubyRunConfiguration(final Project project, final ConfigurationFactory factory, final String name)
	{
		super(project, factory, name);
	}

	@Override
	public AbstractRubyRunConfigurationOptions getOptions()
	{
		return (AbstractRubyRunConfigurationOptions) super.getOptions();
	}

	/**
	 * Converts the interpreter, module, working directory and environment of an old configuration element.
	 */
	protected static void convertLegacyCommonState(LegacyRunConfigurationConverter converter,
			RubyLegacyRunConfigurationState state,
			AbstractRubyRunConfigurationOptions options)
	{
		options.setRubyArgs(state.get(RUBY_ARGS));
		options.setModuleName(state.get(MODULE_NAME));
		options.setUseAlternativeSdk(state.getBoolean(SHOULD_USE_SDK));
		options.setAlternativeSdkName(state.get(ALTERN_SDK_NAME));

		converter.addFragment(new WorkingDirectoryFragmentState(state.get(WORK_DIR)));
		converter.addFragment(new EnvironmentFragmentState(converter.readEnvs(), state.getBoolean(PASS_PARENT_ENVS)));
	}

	/**
	 * Converts the test target shared by test configurations of an old configuration element.
	 */
	protected static void convertLegacyTestTargetState(RubyLegacyRunConfigurationState state, AbstractRTestsRunConfigurationOptions options)
	{
		options.setTestsFolderPath(state.get(TESTS_FOLDER_PATH));
		options.setTestScriptPath(state.get(TEST_SCRIPT_PATH));
		options.setTestFileMask(state.get(TEST_FILE_MASK));
		String testType = state.get(TEST_TEST_TYPE);
		options.setTestType(testType != null ? parseTestType(testType).name() : null);
	}

	protected static TestType parseTestType(@Nullable String value)
	{
		if(value != null)
		{
			for(TestType testType : TestType.values())
			{
				if(testType.name().equals(value))
				{
					return testType;
				}
			}
		}
		return TestType.TEST_SCRIPT;
	}

	@Nonnull
	public Module[] getModules()
	{
		return RModuleUtil.getAllModulesWithRubySupport(getProject());
	}

	@Override
	@Nullable
	public Module getModule()
	{
		return findModuleByName(getModuleName());
	}

	@Nullable
	public String getModuleName()
	{
		return getOptions().getModuleName();
	}

	@Override
	public String getRubyArgs()
	{
		return StringUtil.notNullize(getOptions().getRubyArgs());
	}

	@Nullable
	public Sdk getSdk()
	{
		if(shouldUseAlternativeSdk())
		{
			return getAlternativeSdk();
		}
		final Module module = getModule();
		if(module != null)
		{
			return RModuleUtil.getModuleOrJRubyFacetSdk(module);
		}
		return null;
	}

	@Override
	@Nonnull
	public String getWorkingDirectory()
	{
		return StringUtil.notNullize(WorkingDirectoryAware.super.getWorkingDirectory());
	}

	@Override
	public boolean isGeneratedName()
	{
		return getName().equals(SUGGESTED_NAME);
	}

	@Override
	public void setModule(@Nullable final Module module)
	{
		setModuleName(module != null ? module.getName() : null);
	}

	public void setModuleName(@Nullable String moduleName)
	{
		getOptions().setModuleName(moduleName);
	}

	@Override
	public void setRubyArgs(@Nullable String rubyArgs)
	{
		getOptions().setRubyArgs(rubyArgs);
	}

	@Override
	public void setWorkingDirectory(@Nullable final String dir)
	{
		WorkingDirectoryAware.super.setWorkingDirectory(dir);
	}

	@Override
	public String suggestedName()
	{
		return SUGGESTED_NAME;
	}

	@Nullable
	private Module findModuleByName(@Nullable final String name)
	{
		if(name == null)
		{
			return null;
		}

		final Module module = ModuleManager.getInstance(getProject()).findModuleByName(name);
		return module != null && !module.isDisposed() ? module : null;
	}

	public String getAlternativeSdkName()
	{
		return StringUtil.notNullize(getOptions().getAlternativeSdkName());
	}

	@Override
	public Sdk getAlternativeSdk()
	{
		final String sdkName = getOptions().getAlternativeSdkName();
		if(sdkName == null)
		{
			return null;
		}
		final Sdk sdk = SdkTable.getInstance().findSdk(sdkName);
		return RubySdkUtil.isSDKValid(sdk) ? sdk : null;
	}

	@Override
	public void setAlternativeSdk(@Nullable final Sdk sdk)
	{
		setAlternativeSdkName(sdk != null ? sdk.getName() : null);
	}

	public void setAlternativeSdkName(@Nullable final String sdkName)
	{
		getOptions().setAlternativeSdkName(sdkName);
	}

	protected abstract void validateConfiguration(final boolean isExecution) throws Exception;

	@Override
	public void checkConfiguration() throws RuntimeConfigurationException
	{
		try
		{
			validateConfiguration(false);
		}
		catch(RuntimeConfigurationException ee)
		{
			throw ee;
		}
		catch(Exception e)
		{
			throw new RuntimeConfigurationException(e.getMessage());
		}
	}

	@Override
	public boolean shouldUseAlternativeSdk()
	{
		return getOptions().isUseAlternativeSdk();
	}

	@Override
	public void setShouldUseAlternativeSdk(final boolean shouldUseAlternativeSdk)
	{
		getOptions().setUseAlternativeSdk(shouldUseAlternativeSdk);
	}

	@Override
	public Map<String, String> getEnvs()
	{
		return EnvironmentAware.super.getEnvs();
	}

	@Override
	public void setEnvs(@Nonnull final Map<String, String> envs)
	{
		EnvironmentAware.super.setEnvs(envs);
	}

	@Override
	public boolean isPassParentEnvs()
	{
		return EnvironmentAware.super.isPassParentEnvs();
	}

	@Override
	public void setPassParentEnvs(final boolean passParentEnvs)
	{
		EnvironmentAware.super.setPassParentEnvs(passParentEnvs);
	}

	public enum TestType
	{
		ALL_IN_FOLDER,
		TEST_SCRIPT,
		TEST_CLASS,
		TEST_METHOD,
	}
}
