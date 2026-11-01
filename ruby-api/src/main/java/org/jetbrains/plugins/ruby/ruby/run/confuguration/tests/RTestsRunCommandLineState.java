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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import jakarta.annotation.Nullable;

import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import consulo.execution.runner.ExecutionEnvironment;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 19.07.2007
 */
public class RTestsRunCommandLineState extends AbstractRTestsCommandLineState
{
	private static final String REPORTER = "consulo_test_reporter.rb";
	/**
	 * Requires the test files of a folder: {@code -- <folder> <mask>}
	 */
	private static final String REQUIRE_FOLDER_SCRIPT = "Dir.glob(File.join(ARGV.shift, ARGV.shift)).sort.each { |f| require File.expand_path(f) unless File.directory?(f) }";

	private final RTestsRunConfiguration myConfig;

	public RTestsRunCommandLineState(RTestsRunConfiguration config, ExecutionEnvironment executionEnvironment)
	{
		super(config, executionEnvironment);
		myConfig = config;
	}

	@Override
	public GeneralCommandLine createCommandLine() throws ExecutionException
	{
		final GeneralCommandLine commandLine = createTestDefaultCmdLine(myConfig);

		final Path root = addBundler(commandLine, myConfig);
		if(root != null)
		{
			for(String directory : new String[]{"test", "lib"})
			{
				if(Files.isDirectory(root.resolve(directory)))
				{
					commandLine.addParameter("-I" + root.resolve(directory));
				}
			}
		}
		commandLine.addParameter("-r" + getTestingScript(REPORTER));

		switch(myConfig.getTestType())
		{
			case ALL_IN_FOLDER:
				commandLine.addParameters("-e", REQUIRE_FOLDER_SCRIPT, "--", myConfig.getTestsFolderPath(), myConfig.getTestFileMask());
				break;
			case TEST_CLASS:
				commandLine.addParameter(myConfig.getTestScriptPath());
				if(isTestUnit(myConfig.getTestScriptPath()))
				{
					commandLine.addParameters("-t", myConfig.getTestQualifiedClassName());
				}
				else
				{
					commandLine.addParameters("-n", "/^" + myConfig.getTestQualifiedClassName() + "#/");
				}
				break;
			case TEST_METHOD:
				commandLine.addParameters(myConfig.getTestScriptPath(), "-n", myConfig.getTestMethodName());
				break;
			case TEST_SCRIPT:
				commandLine.addParameter(myConfig.getTestScriptPath());
				break;
		}
		return commandLine;
	}

	/**
	 * test-unit selects a test case with -t, minitest by a name pattern
	 */
	private static boolean isTestUnit(@Nullable final String script)
	{
		if(script == null)
		{
			return false;
		}
		try
		{
			final String text = Files.readString(Path.of(script));
			return text.contains("test/unit") || text.contains("Test::Unit::TestCase");
		}
		catch(IOException e)
		{
			return false;
		}
	}
}
