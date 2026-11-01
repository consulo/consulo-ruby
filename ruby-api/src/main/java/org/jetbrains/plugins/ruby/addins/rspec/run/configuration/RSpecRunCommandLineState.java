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

import consulo.execution.runner.ExecutionEnvironment;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.rubyScript.RubyRunCommandLineState;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.tests.AbstractRTestsCommandLineState;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 19.07.2007
 */
public class RSpecRunCommandLineState extends AbstractRTestsCommandLineState
{
	private static final String FORMATTER = "consulo_rspec_formatter.rb";
	private static final String FORMATTER_CLASS = "ConsuloRSpecFormatter";
	/**
	 * The rspec executable of the rspec-core gem, whether or not the gem bin directory is on the PATH
	 */
	private static final String RSPEC_SCRIPT = "load Gem.bin_path('rspec-core', 'rspec')";

	private final RSpecRunConfiguration myConfig;

	public RSpecRunCommandLineState(final RSpecRunConfiguration config, ExecutionEnvironment executionEnvironment)
	{
		super(config, executionEnvironment);
		myConfig = config;
	}

	@Override
	public GeneralCommandLine createCommandLine() throws ExecutionException
	{
		final GeneralCommandLine commandLine = createTestDefaultCmdLine(myConfig);
		addBundler(commandLine, myConfig);

		commandLine.addParameters("-e", RSPEC_SCRIPT, "--");
		commandLine.addParameters("--require", getTestingScript(FORMATTER), "--format", FORMATTER_CLASS);
		RubyRunCommandLineState.addParams(commandLine, myConfig.getSpecArgs());

		switch(myConfig.getTestType())
		{
			case ALL_IN_FOLDER:
				commandLine.addParameters("--pattern", myConfig.getTestFileMask(), myConfig.getTestsFolderPath());
				break;
			case TEST_METHOD:
				commandLine.addParameter(myConfig.getTestScriptPath() + ":" + myConfig.getExampleLine());
				break;
			default:
				commandLine.addParameter(myConfig.getTestScriptPath());
				break;
		}
		return commandLine;
	}
}
