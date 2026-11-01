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

package org.jetbrains.plugins.ruby.ruby.run;

import consulo.application.util.SystemInfo;
import consulo.content.bundle.Sdk;
import consulo.execution.ui.console.Filter;
import consulo.logging.Logger;
import consulo.process.ExecutionException;
import consulo.process.event.ProcessListener;
import consulo.project.Project;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.awt.Messages;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.RubyUtil;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil;
import org.jetbrains.plugins.ruby.support.utils.OSUtil;
import org.jetbrains.plugins.ruby.support.utils.VirtualFileUtil;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: oleg, Roman Chernyatchik
 * Date: 20.07.2006
 */
public class RubyScriptRunner
{
	private static Logger LOG = Logger.getInstance(RubyScriptRunner.class.getName());
	@NonNls
	private static final String BAT_SUFFIX = ".bat";
	@NonNls
	private static final String SH_SUFFIX = ".sh";
	private static final Output EMPTY_OUPTUT = new Output(TextUtil.EMPTY_STRING, TextUtil.EMPTY_STRING);

	/**
	 * Checks if system script can be found
	 *
	 * @param sdk        Ruby sdk
	 * @param scriptName system script name
	 * @return true if script or script.bat or script.sh can be found in ruby sdk bin folder
	 */
	public static boolean isSystemScriptValid(@Nonnull final Sdk sdk, @Nonnull final String scriptName)
	{
		return getSystemScriptCommand(sdk, scriptName, false) != null;
	}

	/**
	 * Returns output after execution.
	 *
	 * @param sdk              Ruby sdk
	 * @param project          Project
	 * @param scriptPath       Path for ruby script to execute
	 * @param workingDir       working directory
	 * @param mode             Execution mode
	 * @param showStdErrErrors If true, all data from stderr will be shown as errors in Message tab. In this case project must be not null!
	 * @param errorTitle       If showStdErrors is set this title will be used in console. If null, standart error title will be used.
	 * @param arguments        Script arguments
	 * @return Output
	 */
	@Nonnull
	public static Output runRubyScript(@Nullable final Sdk sdk, @Nullable final Project project, @Nonnull final String scriptPath, @Nullable final String workingDir, @Nonnull final Runner.ExecutionMode mode, final boolean showStdErrErrors, @Nullable final String errorTitle, @Nonnull final String... arguments)
	{

		try
		{
			validateSDK(sdk);
		}
		catch(ExecutionException e)
		{
			showExecutionErrorDialog(e);
			return EMPTY_OUPTUT;
		}
		assert sdk != null;

		String[] commands = new String[arguments.length + 2];
		commands[0] = RubySdkUtil.getVMExecutablePath(sdk);
		commands[1] = scriptPath;
		System.arraycopy(arguments, 0, commands, 2, arguments.length);

		final String errTitle = errorTitle != null ? errorTitle : RBundle.message("execution.error.title.abstract.script", scriptPath);
		return Runner.runInPathAndShowErrors(workingDir, project, mode, showStdErrErrors, errTitle, commands);
	}


	public static Output runSystemCommand(@Nonnull final Sdk sdk, @Nullable final String additionalPath, @Nullable final String workingDirectory, @Nonnull final String... arguments)
	{
		try
		{
			validateSDK(sdk);
			@NonNls
			final String argument = "-e exec '" + TextUtil.concat(arguments) + "'";
			return Runner.execute(Runner.createAndSetupCmdLine(additionalPath, workingDirectory, null, true, RubySdkUtil.getVMExecutablePath(sdk), argument));
		}
		catch(ExecutionException e)
		{
			return new Output(TextUtil.EMPTY_STRING, e.getMessage());
		}
	}

	/**
	 * Runs ruby system script in console, i.e. script that placed in ruby bin directory
	 *
	 * @param processListener       User defined ProcessListener
	 * @param consoleFilters        Output filter
	 * @param userActions           if these actions are not null, its will be added to console toolbar
	 * @param runInBackgroundThread Run operation in background thread and show modal dialog
	 * @param consoleTitle          Title for console
	 * @param provider              Provides commandline arguments
	 * @param descFactory           User Factory for creating non default run content descriptors
	 * @param workingDir            Working directory
	 * @param sdk                   Ruby Sdk
	 * @param project               Idea Project
	 */
	public static void runRubyScriptInCosole(@Nullable final ProcessListener processListener, @Nullable final Filter[] consoleFilters, @Nullable final AnAction[] userActions, final boolean runInBackgroundThread, @Nonnull final String consoleTitle, @Nonnull final CommandLineArgumentsProvider provider, @Nullable final RunContentDescriptorFactory descFactory, @Nullable final String workingDir, @Nullable final Sdk sdk, @Nonnull final Project project)
	{

		try
		{
			validateSDK(sdk);
		}
		catch(ExecutionException e)
		{
			showExecutionErrorDialog(e);
			return;
		}

		final String[] params = getVMDefaultParams(sdk);

		ConsoleRunner.run(project, processListener, consoleFilters, userActions, runInBackgroundThread, consoleTitle, workingDir, new RubyScriptRunnerArgumentsProvider(params, provider, null), descFactory);
	}

	@Nonnull
	public static String[] getVMDefaultParams(Sdk sdk)
	{
		final String[] params = new String[RubyUtil.RUN_IN_CONSOLE_HACK_ARGUMENTS.length + 1];
		//noinspection ConstantConditions
		params[0] = RubySdkUtil.getVMExecutablePath(sdk);
		System.arraycopy(RubyUtil.RUN_IN_CONSOLE_HACK_ARGUMENTS, 0, params, 1, RubyUtil.RUN_IN_CONSOLE_HACK_ARGUMENTS.length);
		return params;
	}

	/**
	 * Returns out after scriptSource run.
	 *
	 * @param rubyExe      Path to ruby executable
	 * @param scriptSource script source to tun
	 * @param rubyArgs     ruby Arguments
	 * @param scriptArgs   script arguments
	 * @return Out object
	 */
	@Nonnull
	public static Output runScriptFromSource(final String rubyExe, final String[] rubyArgs, final String scriptSource, final String[] scriptArgs)
	{
		Output result = null;
		File scriptFile = null;
		try
		{
			// Writing source to the temp file
			scriptFile = File.createTempFile("script", ".rb");
			PrintStream out = new PrintStream(scriptFile);
			out.print(scriptSource);
			out.close();
			String scriptPath = scriptFile.getAbsolutePath();
			result = Runner.run(splitNonEmpty(rubyExe, rubyArgs, scriptPath, scriptArgs));
		}
		catch(Exception e)
		{
			LOG.error(e);
		}
		finally
		{
			if(scriptFile.exists())
			{
				scriptFile.delete();
			}
		}
		return result;
	}

	/**
	 * Runs ruby system script, i.e. script that placed in ruby bin directory
	 *
	 * @param sdk                  Ruby sdk
	 * @param project              Project
	 * @param scriptName           system script name
	 * @param workingDir           Working directory, null to inherit parent add home directory
	 * @param runWithModalProgress If true scrtipt will be run under modal cancellable dialog
	 * @param progressTitle        Progress dialog title
	 * @param showStdErrErrors     If true, all data from stderr will be shown as errors in Message tab
	 * @param errorTitle           Title for Message tab
	 * @param arguments            Command to execute @return Output
	 * @return Script output
	 */
	//    @NotNull
	//    public static Output runSystemScript(final Sdk sdk,
	//                                         final Project project, final String scriptName, final String workingDir,
	//                                         final boolean runWithModalProgress, @Nullable final String progressTitle,
	//                                         final boolean showStdErrErrors, final String errorTitle,
	//                                         final String... arguments) {
	//        final String[] scriptCommands = getSystemScriptCommand(sdk, scriptName, true);
	//        if (scriptCommands == null) {
	//            return EMPTY_OUPTUT;
	//        }
	//        ArrayList<String> commandList = new ArrayList<String>();
	//        commandList.addAll(Arrays.asList(scriptCommands));
	//
	//        commandList.addAll(Arrays.asList(arguments));
	//
	//        final String[] commands = commandList.toArray(new String[commandList.size()]);
	//        return showStdErrErrors || runWithModalProgress
	//                ? Runner.runInPathAndShowErrors(workingDir, project, runWithModalProgress, progressTitle, showStdErrErrors, errorTitle, commands)
	//                : Runner.runInPath(workingDir, commands);
	//    }

	/**
	 * Checks if string is not empty and adds it to given list
	 *
	 * @param list List of strings
	 * @param s    Given string
	 */
	private static void addIfNotEmpty(final List<String> list, final String s)
	{
		if(s != null && !s.trim().equals(""))
		{
			list.add(s);
		}
	}

	/**
	 * Returns system script command
	 *
	 * @param sdk        Ruby sdk
	 * @param scriptName system script name
	 * @param showErrMsg determinates if error message dialog must be shown
	 * @return true if script or script.bat or script.sh can be found in ruby sdk bin folder
	 */
	@Nullable
	public static String[] getSystemScriptCommand(@Nullable final Sdk sdk, @Nonnull final String scriptName, final boolean showErrMsg)
	{
		try
		{
			validateSDK(sdk);

			//noinspection ConstantConditions
			final String binFolder = RubySdkUtil.getBinPath(sdk) + VirtualFileUtil.VFS_PATH_SEPARATOR;

			// Try to find script under SDK bin path
			if(checkIfPathExists(binFolder + scriptName))
			{
				final String[] defaultParams = getVMDefaultParams(sdk);
				final String[] params = new String[defaultParams.length + 1];
				System.arraycopy(defaultParams, 0, params, 0, defaultParams.length);
				params[params.length - 1] = binFolder + scriptName;

				return params;
			}
			// Try to find executable under SDK bin path
			if(SystemInfo.isWindows && checkIfPathExists(binFolder + scriptName + BAT_SUFFIX))
			{
				return new String[]{binFolder + scriptName + BAT_SUFFIX};
			}
			if(!SystemInfo.isWindows && checkIfPathExists(binFolder + scriptName + SH_SUFFIX))
			{
				return new String[]{binFolder + scriptName + SH_SUFFIX};
			}
			// Try to find executable in PATH environment
			final String path = OSUtil.findExecutableByName(SystemInfo.isWindows ? scriptName + BAT_SUFFIX : scriptName);
			if(path != null)
			{
				return new String[]{path};
			}
			throw new ExecutionException(RBundle.message("execution.error.no.executable.cmd.for.script", scriptName));
		}
		catch(ExecutionException e)
		{
			if(showErrMsg)
			{
				showExecutionErrorDialog(e);
			}
			return null;
		}
	}

	/**
	 * @param path Path to check
	 * @return true, if path exists
	 */
	private static boolean checkIfPathExists(@Nonnull final String path)
	{
		File file = new File(path);
		return file.exists() && file.isFile();
	}

	public static void showExecutionErrorDialog(final ExecutionException e)
	{
		Messages.showErrorDialog(e.getMessage(), RBundle.message("execution.error.title"));
	}

	private static String[] splitNonEmpty(final String rubyExe, final String[] rubyArgs, final String path, final String[] scriptArgs)
	{
		ArrayList<String> commands = new ArrayList<String>();
		addIfNotEmpty(commands, rubyExe);
		for(String s : rubyArgs)
		{
			addIfNotEmpty(commands, s);
		}
		addIfNotEmpty(commands, path);
		for(String s : scriptArgs)
		{
			addIfNotEmpty(commands, s);
		}
		return commands.toArray(new String[commands.size()]);
	}

	public static void validateSDK(@Nullable final Sdk sdk) throws ExecutionException
	{
		if(sdk == null)
		{
			throw new ExecutionException(RBundle.message("sdk.no.specified"));
		}

		if(!RubySdkUtil.isKindOfRubySDK(sdk))
		{
			throw new ExecutionException(RBundle.message("sdk.error.isnt.valid", sdk.getName()));
		}

		if(!RubySdkUtil.isSDKHomeExist(sdk))
		{
			throw new ExecutionException(RBundle.message("sdk.error.homepath.doesnt.exists", sdk.getName()));
		}
	}

	private static final String IO_EXCEPTION_PREFIX = "java.io.IOException:";

	public static void showErrorMessage(Project project, String errorTitle, Exception exp)
	{
		String errorMessage = exp.getMessage();
		if(errorMessage != null && errorMessage.startsWith(IO_EXCEPTION_PREFIX))
		{
			errorMessage = errorMessage.substring(IO_EXCEPTION_PREFIX.length());
		}
		if(errorMessage == null || errorMessage.length() == 0)
		{
			errorMessage = exp.toString();
		}
		Messages.showErrorDialog(project, errorMessage, errorTitle);
	}
}
