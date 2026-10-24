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

package org.jetbrains.plugins.ruby.rails.run.configuration.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import jakarta.annotation.Nonnull;

import consulo.execution.executor.Executor;
import consulo.execution.runner.ExecutionEnvironment;
import jakarta.annotation.Nullable;

import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.rails.RailsConstants;
import org.jetbrains.plugins.ruby.rails.RailsUtil;
import org.jetbrains.plugins.ruby.rails.facet.RailsFacetUtil;
import org.jetbrains.plugins.ruby.rails.run.configuration.RailsRunConfigurationUtil;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyArgumentsFragment;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyLegacyRunConfigurationState;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationUtil;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubySdkFragment;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.rubyScript.RubyRunConfiguration;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.rubyScript.RubyRunConfigurationOptions;
import org.jetbrains.plugins.ruby.support.utils.VirtualFileUtil;
import consulo.process.ExecutionException;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.RunConfigurationOptions;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.module.Module;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import consulo.util.lang.text.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 04.08.2007
 */
public class RailsServerRunConfiguration extends RubyRunConfiguration implements RailsServerRunConfigurationParams
{
	public static final String DEFAULT_SERVER = "Default";
	public static final String RAILS_SERVER_CONFIG_SETTINGS_ID = "RAILS_SERVER_CONFIG_SETTINGS_ID";

	private static final String CHOOSE_MANUALLY = "CHOOSE_MANUALLY";
	private static final String PORT = "PORT";
	private static final String IP = "IP";
	private static final String SERVER_TYPE = "RAILS_SERVER_TYPE";
	private static final String ENVIRONMENT_TYPE = "ENVIRONMENT_TYPE";

	public RailsServerRunConfiguration(final Project project, final ConfigurationFactory factory, final String name)
	{
		super(project, factory, name);
	}

	@Override
	protected Class<? extends RunConfigurationOptions> getOptionsClass()
	{
		return RailsServerRunConfigurationOptions.class;
	}

	@Override
	public RailsServerRunConfigurationOptions getOptions()
	{
		return (RailsServerRunConfigurationOptions) super.getOptions();
	}

	@Override
	protected String getLegacySettingsId()
	{
		return RAILS_SERVER_CONFIG_SETTINGS_ID;
	}

	@Override
	protected RubyRunConfigurationOptions createLegacyOptions(RubyLegacyRunConfigurationState state)
	{
		RailsServerRunConfigurationOptions options = new RailsServerRunConfigurationOptions();
		String port = state.get(PORT);
		options.setPort(port == null ? "0" : port);
		options.setIpAddress(state.get(IP));
		options.setChoosePortManually(state.getBoolean(CHOOSE_MANUALLY));
		options.setServerType(state.get(SERVER_TYPE));
		options.setEnvironmentType(state.get(ENVIRONMENT_TYPE));
		return options;
	}

	@Override
	public List<RunConfigurationFragment<?>> createFragments()
	{
		return List.of(
				new RailsServerFragment(),
				new RubySdkFragment<>(),
				new RubyArgumentsFragment<>()
		);
	}

	/**
	 * @param module Rails or Java Module
	 * @return Work directory if Rails Application Home Directory or null(if no rails in module)
	 */
	@Nullable
	public static String getRailsWorkDirByModule(@Nonnull final Module module)
	{
		return RailsFacetUtil.getRailsAppHomeDirPath(module);
	}

	/**
	 * @param module Ruby or Java Module
	 * @return Server script path corresponding to specified module : {rails application home dir}/script/server
	 */
	@Nullable
	public static String getServerScriptPathByModule(final Module module)
	{
		final String homeDir = RailsFacetUtil.getRailsAppHomeDirPath(module);
		if(homeDir == null)
		{
			return null;
		}
		return VirtualFileUtil.buildSystemIndependentPath(homeDir, RailsConstants.SERVER_SCRIPT);
	}

	@Override
	public boolean isChoosePortManually()
	{
		return getOptions().isChoosePortManually();
	}

	@Override
	public void setChoosePortManually(final boolean choosePortManually)
	{
		getOptions().setChoosePortManually(choosePortManually);
	}

	@Override
	public String getPort()
	{
		return StringUtil.notNullize(getOptions().getPort());
	}

	@Override
	public void setPort(final String port)
	{
		getOptions().setPort(port == null ? "0" : port);
	}

	@Override
	@Nonnull
	public String getIPAddr()
	{
		return StringUtil.notNullize(getOptions().getIpAddress());
	}

	@Override
	@Nonnull
	public String getServerType()
	{
		return StringUtil.notNullize(getOptions().getServerType(), DEFAULT_SERVER);
	}

	@Override
	public void setServerType(@Nonnull final String type)
	{
		getOptions().setServerType(type);
	}

	@Override
	public void setIPAddr(final String ip)
	{
		getOptions().setIpAddress(ip);
	}

	/**
	 * Valid Modules for Rails Server configuration should contain Rails/JRails facet
	 *
	 * @return valid rails modules
	 */
	@Override
	@Nonnull
	public Module[] getModules()
	{
		return RailsUtil.getAllModulesWithRailsSupport(getProject());
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


		return new RailsServerRunCommandLineState(this, executionEnvironment);

	}

	@Override
	protected void validateConfiguration(final boolean isExecution) throws Exception
	{
		super.validateConfiguration(isExecution);

		RailsRunConfigurationUtil.inspectRailsSDK(this, isExecution);
		inspectPort(isExecution);
		inspectIPAddr(isExecution);
	}

	private void inspectIPAddr(final boolean isExecution) throws Exception
	{
		final String msg = "run.configuration.script.ip.bad.format";
		final String ip_str = getIPAddr();
		final consulo.util.lang.text.StringTokenizer st = new StringTokenizer(ip_str, ".", true);
		if(st.countTokens() != 7)
		{
			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message(msg), isExecution);
		}
		// first byte
		checkNumberFormat(isExecution, st.nextToken(), true, msg);
		checkPoint(isExecution, st.nextToken());
		// secont byte
		checkNumberFormat(isExecution, st.nextToken(), true, msg);
		checkPoint(isExecution, st.nextToken());
		// third byte
		checkNumberFormat(isExecution, st.nextToken(), true, msg);
		checkPoint(isExecution, st.nextToken());
		// fourth byte
		checkNumberFormat(isExecution, st.nextToken(), true, msg);
	}

	private void inspectPort(final boolean isExecution) throws Exception
	{
		if(isChoosePortManually())
		{
			final String port_str = getPort();

			checkNumberFormat(isExecution, port_str, false, "run.configuration.script.port.bad.format");

			try
			{
				final ServerSocket s = new ServerSocket(Integer.valueOf(port_str));
				s.close();
			}
			catch(IOException e)
			{
				throw new ExecutionException(RBundle.message("run.configuration.script.port.is.busy", port_str));
			}
		}
	}

	private void checkNumberFormat(final boolean isExecution, final String port_str, final boolean isByte, final String msg) throws Exception
	{
		Integer port = null;
		try
		{
			port = Integer.valueOf(port_str);
		}
		catch(NumberFormatException e)
		{
			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message(msg), isExecution);
		}
		assert port != null;
		if(port < 0 || port > (isByte ? 255 : 65535))
		{
			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message(msg), isExecution);
		}
	}

	private void checkPoint(final boolean isExecution, final String port_str) throws Exception
	{
		if(port_str.equals("."))
		{
			return;
		}
		RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.script.ip.bad.format"), isExecution);
	}

	@Override
	public RailsEnvironmentType getRailsEnvironmentType()
	{
		String environmentType = getOptions().getEnvironmentType();
		if(environmentType != null)
		{
			for(RailsEnvironmentType type : RailsEnvironmentType.values())
			{
				if(type.name().equals(environmentType))
				{
					return type;
				}
			}
		}
		return RailsEnvironmentType.DEVELOPMENT;
	}

	@Override
	public void setRailsEnvironmentType(final RailsEnvironmentType railsEnvironmentType)
	{
		getOptions().setEnvironmentType(railsEnvironmentType.name());
	}

	public enum RailsEnvironmentType
	{
		DEVELOPMENT,
		PRODUCTION,
		TEST;

		public String getParamName()
		{
			return super.toString().toLowerCase();
		}
	}
}
