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

import consulo.execution.runner.ExecutionEnvironment;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import consulo.util.io.NetUtil;
import org.jetbrains.plugins.ruby.rails.RailsConstants;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.rubyScript.RubyRunCommandLineState;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 04.08.2007
 */
public class RailsServerRunCommandLineState extends RubyRunCommandLineState
{
	public RailsServerRunCommandLineState(final RailsServerRunConfiguration config, ExecutionEnvironment executionEnvironment)
	{
		super(config, executionEnvironment);
	}

	@Override
	public GeneralCommandLine createCommandLine() throws ExecutionException
	{
		final GeneralCommandLine commandLine = createGeneralDefaultCmdLine(myConfig);
		final RailsServerRunConfiguration config = (RailsServerRunConfiguration) myConfig;

		int port_int;
		if(config.isChoosePortManually())
		{
			port_int = Integer.valueOf(config.getPort());
		}
		else
		{
			try
			{
				port_int = NetUtil.findAvailableSocketPort();
			}
			catch(IOException e)
			{
				throw new ExecutionException("Error occured while searching for free port");
			}
		}

		//interpretator params
		addParams(commandLine, myConfig.getRubyArgs());
		// bin/rails server
		commandLine.addParameter(myConfig.getScriptPath());
		commandLine.addParameter(RailsConstants.SERVER_COMMAND);

		// the server of the bundle (puma) unless one is chosen
		final String server = config.getServerType();
		if(!server.isBlank() && !RailsServerRunConfiguration.DEFAULT_SERVER.equals(server))
		{
			commandLine.addParameters(RailsConstants.PARAM_SERVER_USING, server);
		}

		//server port
		commandLine.addParameter(RailsConstants.PARAM_SERVER_PORT);
		commandLine.addParameter(String.valueOf(port_int));

		//server ip address
		commandLine.addParameter(RailsConstants.PARAM_SERVER_IP);
		commandLine.addParameter(config.getIPAddr());

		//server environment
		commandLine.addParameter(RailsConstants.PARAM_SERVER_ENVIRONMENT);
		commandLine.addParameter(config.getRailsEnvironmentType().getParamName());

		//server args
		addParams(commandLine, myConfig.getScriptArgs());

		return commandLine;
	}
}
