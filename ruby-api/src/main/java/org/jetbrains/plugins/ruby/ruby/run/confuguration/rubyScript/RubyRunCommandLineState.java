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

import consulo.execution.ui.console.TextConsoleBuilder;
import consulo.execution.ui.console.TextConsoleBuilderFactory;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.ColouredCommandLineState;
import org.jetbrains.plugins.ruby.ruby.run.filters.RFileLinksFilter;
import org.jetbrains.plugins.ruby.ruby.run.filters.RStackTraceFilter;
import org.jetbrains.plugins.ruby.support.utils.VirtualFileUtil;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import consulo.execution.runner.ExecutionEnvironment;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: 21.08.2006
 */

public class RubyRunCommandLineState extends ColouredCommandLineState
{

	final protected RubyRunConfiguration myConfig;

	public RubyRunCommandLineState(final @Nonnull RubyRunConfiguration config, ExecutionEnvironment executionEnvironment)
	{
		super(executionEnvironment);

		myConfig = config;

		final Project project = config.getProject();

		final TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
		setConsoleBuilder(consoleBuilder);

		final String scriptDir = VirtualFileUtil.getParentDir(config.getScriptPath());

		addFilters(config, project, consoleBuilder, scriptDir);
	}

	public static void addParams(GeneralCommandLine cmdLine, String argsString)
	{
		String[] args = argsString.split(" ");
		for(String arg : args)
		{
			if(arg.length() > 0)
			{
				cmdLine.addParameter(arg);
			}
		}
	}

	@Override
	public GeneralCommandLine createCommandLine() throws ExecutionException
	{
		final GeneralCommandLine commandLine = createGeneralDefaultCmdLine(myConfig);

		addParams(commandLine, myConfig.getRubyArgs());
		commandLine.addParameter(myConfig.getScriptPath());
		addParams(commandLine, myConfig.getScriptArgs());

		return commandLine;
	}

	private void addFilters(final RubyRunConfiguration config, final Project project, final TextConsoleBuilder consoleBuilder, final String scriptDir)
	{
		consoleBuilder.addFilter(new RStackTraceFilter(project, scriptDir));
		consoleBuilder.addFilter(new RFileLinksFilter(config.getModule()));
	}

}

