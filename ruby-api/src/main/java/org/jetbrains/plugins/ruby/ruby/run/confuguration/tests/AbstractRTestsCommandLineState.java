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

import consulo.execution.CantRunException;
import consulo.execution.DefaultExecutionResult;
import consulo.execution.ExecutionResult;
import consulo.execution.test.sm.SMTestRunnerConnectionUtil;
import consulo.execution.test.sm.runner.SMTRunnerConsoleProperties;
import consulo.execution.test.ui.BaseTestsOutputConsoleView;
import consulo.process.ProcessHandler;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.runner.ProgramRunner;
import consulo.process.ExecutionException;
import consulo.execution.executor.Executor;
import consulo.process.cmd.GeneralCommandLine;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.AbstractRubyRunConfiguration;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.ColouredCommandLineState;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.rubyScript.RubyRunCommandLineState;
import org.jetbrains.plugins.ruby.ruby.run.filters.RFileLinksFilter;
import org.jetbrains.plugins.ruby.ruby.run.filters.RStackTraceFilter;

import consulo.container.plugin.PluginManager;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkType;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: Oct 18, 2007
 */
public abstract class AbstractRTestsCommandLineState extends ColouredCommandLineState
{
	public static final String FRAMEWORK_NAME = "Ruby";

	protected AbstractRTestsCommandLineState(final AbstractRubyRunConfiguration config, ExecutionEnvironment executionEnvironment)
	{
		super(executionEnvironment);
	}

	/**
	 * The tests run with the reporter of the plugin and show in the test tree
	 */
	@Override
	public ExecutionResult execute(@Nonnull Executor executor, @Nonnull ProgramRunner runner) throws ExecutionException
	{
		final AbstractRubyRunConfiguration config = (AbstractRubyRunConfiguration) getEnvironment().getRunProfile();
		final ProcessHandler processHandler = startProcess();

		final SMTRunnerConsoleProperties properties = new SMTRunnerConsoleProperties(config, FRAMEWORK_NAME, executor);
		properties.setIdBasedTestTree(true);
		final BaseTestsOutputConsoleView console = SMTestRunnerConnectionUtil.createAndAttachConsole(FRAMEWORK_NAME, processHandler, properties);
		console.addMessageFilter(new RStackTraceFilter(config.getProject(), config.getWorkingDirectory()));
		console.addMessageFilter(new RFileLinksFilter(config.getModule()));

		return new DefaultExecutionResult(console, processHandler);
	}

	protected GeneralCommandLine createTestDefaultCmdLine(final AbstractRubyRunConfiguration config) throws CantRunException
	{
		final GeneralCommandLine commandLine = createGeneralDefaultCmdLine(config);
		RubyRunCommandLineState.addParams(commandLine, config.getRubyArgs());
		return commandLine;
	}

	/**
	 * Loads the gems of the Gemfile above the tests, as <code>bundle exec</code> does.
	 *
	 * @return the directory of the Gemfile, else the base directory of the tests
	 */
	@Nullable
	protected static <C extends AbstractRubyRunConfiguration & AbstractRTestsRunConfigurationParams> Path addBundler(final GeneralCommandLine commandLine, final C config)
	{
		final Path baseDirectory = getBaseDirectory(config);
		final Path gemfile = baseDirectory == null ? null : findGemfile(baseDirectory);
		if(gemfile != null)
		{
			commandLine.withEnvironment("BUNDLE_GEMFILE", gemfile.toString());
			commandLine.addParameter("-rbundler/setup");
			return gemfile.getParent();
		}
		return baseDirectory;
	}

	/**
	 * @param name a script of the plugin's <code>testing</code> directory
	 */
	protected static String getTestingScript(final String name)
	{
		return new File(PluginManager.getPluginPath(RubySdkType.class), "testing/" + name).getPath();
	}

	@Nullable
	private static <C extends AbstractRubyRunConfiguration & AbstractRTestsRunConfigurationParams> Path getBaseDirectory(final C config)
	{
		final String workingDirectory = config.getWorkingDirectory();
		if(workingDirectory != null && !workingDirectory.isBlank())
		{
			return Path.of(workingDirectory);
		}
		final String script = config.getTestType() == AbstractRubyRunConfiguration.TestType.ALL_IN_FOLDER ? config.getTestsFolderPath() : config.getTestScriptPath();
		return script == null || script.isBlank() ? null : Path.of(script).getParent();
	}

	@Nullable
	private static Path findGemfile(final Path directory)
	{
		for(Path dir = directory.toAbsolutePath(); dir != null; dir = dir.getParent())
		{
			final Path gemfile = dir.resolve("Gemfile");
			if(Files.isRegularFile(gemfile))
			{
				return gemfile;
			}
		}
		return null;
	}
}
