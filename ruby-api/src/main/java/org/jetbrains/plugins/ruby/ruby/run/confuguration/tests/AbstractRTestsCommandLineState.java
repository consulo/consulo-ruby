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

import java.io.File;

import javax.annotation.Nonnull;

import org.jetbrains.plugins.ruby.ruby.run.confuguration.AbstractRubyRunConfiguration;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.ColouredCommandLineState;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.rubyScript.RubyRunCommandLineState;
import org.jetbrains.plugins.ruby.ruby.run.filters.RFileLinksFilter;
import org.jetbrains.plugins.ruby.ruby.run.filters.RStackTraceFilter;
import com.intellij.execution.CantRunException;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: Oct 18, 2007
 */
public abstract class AbstractRTestsCommandLineState extends ColouredCommandLineState
{
	protected File tempFile;

	protected AbstractRTestsCommandLineState(final AbstractRubyRunConfiguration config, ExecutionEnvironment executionEnvironment)
	{
		super(executionEnvironment);
		final Project project = config.getProject();

		final TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
		setConsoleBuilder(consoleBuilder);

		addFilters(config, project, consoleBuilder, config.getWorkingDirectory());

		attachCompilerForJRuby(config);
	}


	@Override
	public ExecutionResult execute(@Nonnull Executor executor, @Nonnull ProgramRunner runner) throws ExecutionException
	{
		final ExecutionResult result = super.execute(executor, runner);
		if(result != null)
		{
			result.getProcessHandler().addProcessListener(new ProcessListener()
			{
				@Override
				public void startNotified(final ProcessEvent event)
				{
				}

				@Override
				public void processTerminated(final ProcessEvent event)
				{
					if(tempFile != null)
					{
						tempFile.delete();
					}
				}

				@Override
				public void processWillTerminate(final ProcessEvent event, final boolean willBeDestroyed)
				{
				}

				@Override
				public void onTextAvailable(final ProcessEvent event, final Key outputType)
				{
				}
			});
		}
		return result;
	}

	protected void addFilters(final AbstractRubyRunConfiguration config, final Project project, final TextConsoleBuilder consoleBuilder, final String scriptDir)
	{
		consoleBuilder.addFilter(new RStackTraceFilter(project, scriptDir));
		consoleBuilder.addFilter(new RFileLinksFilter(config.getModule()));
	}

	protected GeneralCommandLine createTestDefaultCmdLine(final AbstractRubyRunConfiguration config) throws CantRunException
	{
		final GeneralCommandLine commandLine = createGeneralDefaultCmdLine(config);

		RubyRunCommandLineState.addParams(commandLine, config.getRubyArgs());
		return commandLine;
	}
}
