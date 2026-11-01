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

import consulo.application.Application;
import consulo.logging.Logger;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.process.ProcessOutputTypes;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.event.ProcessAdapter;
import consulo.process.event.ProcessEvent;
import consulo.project.Project;
import consulo.util.dataholder.Key;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.settings.RApplicationSettings;
import org.jetbrains.plugins.ruby.support.utils.OSUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static consulo.util.io.FileUtil.toSystemDependentName;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg, Roman Chernyatchik
 * @date: 24.08.2006
 */
public class Runner {
    private static final Logger LOG = Logger.getInstance(Runner.class.getName());

    /**
     * Returns output after execution.
     *
     * @param workingDir working directory
     * @param command    Command to execute @return Output object
     * @return Output
     */
    @Nonnull
    public static Output runInPath(@Nullable final String workingDir, @Nonnull final String... command) {
        return runInPathInternal(workingDir, null, null, false, new Runner.SameThreadMode(10), command);
    }

    public static Output runInPathAndShowErrors(@Nullable final String workingDir, @Nullable Project project, @Nonnull final ExecutionMode mode, final boolean showStdErrErrors, @Nullable final String errorTitle, @Nonnull final String... command) {
        return runInPathInternal(workingDir, project, errorTitle, showStdErrErrors, mode, command);
    }

    /**
     * Returns output after execution.
     *
     * @param workingDir working directory
     * @param project    Project
     * @param errorTitle Title for Message tab
     * @param showErrors If true, all data from stderr will be shown as errors in Message tab. In this case project must be not null!
     * @param mode       Execution mode
     * @param command    Command to execute @return Output object
     * @return Output       Output
     */
    @Nonnull
    private static Output runInPathInternal(@Nullable final String workingDir,
                                            @Nullable Project project,
                                            @Nullable final String errorTitle,
                                            final boolean showErrors,
                                            @Nonnull final ExecutionMode mode,
                                            @Nonnull final String... command) {
        // executing
        final StringBuilder out = new StringBuilder();
        final StringBuilder err = new StringBuilder();
        final GeneralCommandLine commandLine = createProcess(workingDir, command);
        final ProcessHandler osProcessHandler;
        try {
            osProcessHandler = Application.get().getInstance(ProcessHandlerBuilderFactory.class).newBuilder(commandLine).build();
        }
        catch (ExecutionException e) {
            return new Output("", e.getMessage());
        }
        
        osProcessHandler.addProcessListener(new OutputListener(out, err));
        osProcessHandler.startNotify();

        ExecutionHelper.executeExternalProcess(project, osProcessHandler, mode);

        final Output output = new Output(out.toString(), err.toString());
        if (showErrors && !TextUtil.isEmpty(output.getStderr())) {
            assert project != null;
            final String tabName = errorTitle != null ? errorTitle : RBundle.message("exception.text.unknown.error");

            final List<Exception> errorList = new LinkedList<Exception>();
            //noinspection ThrowableInstanceNeverThrown
            errorList.add(new Exception(output.getStderr()));
            ExecutionHelper.showErrors(project, errorList, tabName, null);
        }
        return output;
    }

    @Nonnull
    public static Output execute(@Nonnull final GeneralCommandLine cmdLine) throws ExecutionException {
        final StringBuilder out = new StringBuilder();
        final StringBuilder err = new StringBuilder();

        final ProcessHandler osProcessHandler = Application.get().getInstance(ProcessHandlerBuilderFactory.class).newBuilder(cmdLine).build();

        osProcessHandler.addProcessListener(new OutputListener(out, err));
        osProcessHandler.startNotify();
        osProcessHandler.waitFor();

        return new Output(out.toString(), err.toString());
    }

    /**
     * Returns output after execution.
     *
     * @param command Command to execute
     * @return Output object
     */
    @Nonnull
    public static Output run(@Nonnull final String... command) {
        return runInPath(null, command);
    }

    /**
     * Creates add by command and working directory
     *
     * @param command    add command line
     * @param workingDir add working directory or null, if no special needed
     * @return add
     */
    @Nullable
    public static GeneralCommandLine createProcess(@Nullable final String workingDir, @Nonnull final String... command) {
        final String[] arguments;
        if (command.length > 1) {
            arguments = new String[command.length - 1];
            System.arraycopy(command, 1, arguments, 0, command.length - 1);
        }
        else {
            arguments = new String[0];
        }

        final GeneralCommandLine cmdLine = createAndSetupCmdLine(null, workingDir, null, true, command[0], arguments);
        return cmdLine;
    }

    /**
     * Creates process builder and setups it's commandLine, working directory, enviroment variables
     *
     * @param additionalLoadPath Additional load path
     * @param workingDir         Process working dir
     * @param additionalEnvs     If not null process will be executed with these environment varialbes
     * @param passParentEnvs     If true environment variables of parent process will be added to process's set of environment variables
     * @param executablePath     Path to executable file
     * @param arguments          Process commandLine
     * @return process builder
     */
    public static GeneralCommandLine createAndSetupCmdLine(@Nullable final String additionalLoadPath, @Nullable final String workingDir, @Nullable final Map<String, String> additionalEnvs, final boolean passParentEnvs, @Nonnull final String executablePath, @Nonnull final String... arguments) {
        final GeneralCommandLine cmdLine = new GeneralCommandLine();

        cmdLine.setExePath(toSystemDependentName(executablePath));
        if (workingDir != null) {
            cmdLine.setWorkDirectory(toSystemDependentName(workingDir));
        }
        cmdLine.addParameters(arguments);

        //Parent envs variables
        final Map<String, String> cutstomEnvVariables;
        if (additionalEnvs == null) {
            cutstomEnvVariables = new HashMap<String, String>();
        }
        else {
            cutstomEnvVariables = new HashMap<String, String>(additionalEnvs);
        }
        //Plugin env variables
        getRORPluginExtendedEnvPATH(cutstomEnvVariables);

        //PATH
        if (!TextUtil.isEmpty(additionalLoadPath)) {
            final String PATH_KEY = OSUtil.getPATHenvVariableName();
            final String path = cutstomEnvVariables.get(PATH_KEY);

            //Additional Extention
            //noinspection ConstantConditions
            cutstomEnvVariables.put(PATH_KEY, OSUtil.appendToPATHenvVariable(path, additionalLoadPath));
        }

        //User's custom ENV variables
        final Map<String, String> envParams = new HashMap<String, String>();
        if (passParentEnvs) {
            envParams.putAll(System.getenv());
        }
        envParams.putAll(cutstomEnvVariables);

        //Setting cmdLine params
        cmdLine.getEnvironment().putAll(envParams);
        return cmdLine;
    }

    public static void getRORPluginExtendedEnvPATH(final Map<String, String> env) {
        final String PATH_KEY = OSUtil.getPATHenvVariableName();

        final String path = env.get(PATH_KEY);
        final String pluginAdditionalPath = RApplicationSettings.getInstance().additionalEnvPATH;
        if (!TextUtil.isEmpty(pluginAdditionalPath)) {
            //noinspection ConstantConditions
            env.put(PATH_KEY, OSUtil.appendToPATHenvVariable(path, pluginAdditionalPath));
        }
    }

    public static class OutputListener extends ProcessAdapter {
        private final StringBuilder out;
        private final StringBuilder err;

        public OutputListener(@Nonnull final StringBuilder out, @Nonnull final StringBuilder err) {
            this.out = out;
            this.err = err;
        }

        @Override
        public void onTextAvailable(ProcessEvent event, Key outputType) {
            if (outputType == ProcessOutputTypes.STDOUT) {
                out.append(event.getText());
            }
            if (outputType == ProcessOutputTypes.STDERR) {
                err.append(event.getText());
            }
        }
    }

    public static abstract class ExecutionMode {
        private boolean myCancelable;
        private String myTitle;
        private String myTitle2;
        private boolean myRunWithModal;
        private boolean myRunInBG;
        private Function<Object, Boolean> myShouldCancelFun;
        private final Object CANCEL_FUN_LOCK = new Object();

        public ExecutionMode(final boolean cancelable, @Nullable final String title, @Nullable final String title2, final boolean runInBG, final boolean runWithModal) {
            myCancelable = cancelable;
            myTitle = title;
            myTitle2 = title2;
            myRunInBG = runInBG;
            myRunWithModal = runWithModal;
        }

        @Nullable
        public String getTitle() {
            return myTitle;
        }

        @Nullable
        public String getTitle2() {
            return myTitle2;
        }

        public boolean cancelable() {
            return myCancelable;
        }

        public boolean inBackGround() {
            return myRunInBG;
        }

        public boolean withModalProgress() {
            return myRunWithModal;
        }

        public int getTimeout() {
            // it is ignored
            return -1;
        }

        @Nullable
        /**
         * Runner checks this fun during process running, if returns true, process will be canceled.
         */
        public Function<Object, Boolean> shouldCancelFun() {
            synchronized (CANCEL_FUN_LOCK) {
                return myShouldCancelFun;
            }
        }

        public void setShouldCancelFun(final Function<Object, Boolean> shouldCancelFun) {
            synchronized (CANCEL_FUN_LOCK) {
                myShouldCancelFun = shouldCancelFun;
            }
        }
    }

    /**
     * Process will be run in back ground mode
     */
    public static class BackGroundMode extends ExecutionMode {

        public BackGroundMode(final boolean cancelable, @Nullable final String title) {
            super(cancelable, title, null, true, false);
        }

        public BackGroundMode(@Nullable final String title) {
            this(true, title);
        }
    }

    /**
     * Process will be run in modal dialog
     */
    public static class ModalProgressMode extends ExecutionMode {

        public ModalProgressMode(final boolean cancelable, @Nullable final String title) {
            super(cancelable, title, null, false, true);
        }

        public ModalProgressMode(@Nullable final String title) {
            this(true, title);
        }
    }

    /**
     * Process will be run in the same thread.
     */
    public static class SameThreadMode extends ExecutionMode {
        private final int myTimeout;

        public SameThreadMode(final boolean cancelable, @Nullable final String title2, final int timeout) {
            super(cancelable, null, title2, false, false);
            myTimeout = timeout;
        }

        public SameThreadMode(@Nullable final String title2) {
            this(true, title2, -1);
        }

        /**
         * @param cancelable If process is cancelable
         */
        public SameThreadMode(final boolean cancelable) {
            this(cancelable, null, -1);
        }

        /**
         * @param timeout If less than zero it will be ignored
         */
        public SameThreadMode(final int timeout) {
            this(false, null, timeout);
        }

        public SameThreadMode() {
            this(true);
        }

        @Override
        public int getTimeout() {
            return myTimeout;
        }
    }
}
