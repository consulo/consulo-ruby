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

import consulo.localize.LocalizeValue;
import consulo.application.Application;
import consulo.execution.CantRunException;
import consulo.execution.RuntimeConfigurationException;
import consulo.execution.configuration.CommandLineState;
import consulo.execution.process.ProcessTerminatedListener;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.platform.Platform;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilder;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.process.cmd.GeneralCommandLine;
import consulo.util.lang.StringUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import consulo.ruby.runtime.RubyRuntime;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.run.Runner;
import org.jetbrains.plugins.ruby.settings.RApplicationSettings;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: Nov 7, 2007
 */
public abstract class ColouredCommandLineState extends CommandLineState {
    private static final String RUBYOPT = "RUBYOPT";

    private final List<String> myRequiredLibraries = new ArrayList<>();
    private final Map<String, String> myExtraEnvironment = new LinkedHashMap<>();

    protected ColouredCommandLineState(ExecutionEnvironment environment) {
        super(environment);
    }

    /**
     * Loads a library before the program, whatever the command line runs: a debugger, for example.
     */
    public void addRequiredLibrary(String path) {
        myRequiredLibraries.add(path);
    }

    public void addEnvironment(String name, String value) {
        myExtraEnvironment.put(name, value);
    }

    @Override
    protected ProcessHandler startProcess() throws ExecutionException {
        final GeneralCommandLine cmdLine = createCommandLine();
        cmdLine.withEnvironment(myExtraEnvironment);
        if (!myRequiredLibraries.isEmpty()) {
            // RUBYOPT libraries load after the ones of the command line, bundler/setup included
            StringBuilder rubyOpt = new StringBuilder(StringUtil.notNullize(getInheritedRubyOpt(cmdLine)));
            for (String library : myRequiredLibraries) {
                rubyOpt.append(rubyOpt.isEmpty() ? "" : " ").append("-r").append(library);
            }
            cmdLine.withEnvironment(RUBYOPT, rubyOpt.toString());
        }
        final ProcessHandler processHandler = createOSProcessHandler(cmdLine);
        ProcessTerminatedListener.attach(processHandler);
        return processHandler;
    }

    @Nullable
    private static String getInheritedRubyOpt(GeneralCommandLine cmdLine) {
        String rubyOpt = cmdLine.getEnvironment().get(RUBYOPT);
        if (rubyOpt == null && cmdLine.getParentEnvironmentType() != GeneralCommandLine.ParentEnvironmentType.NONE) {
            rubyOpt = Platform.current().os().getEnvironmentVariable(RUBYOPT);
        }
        return rubyOpt;
    }

    public GeneralCommandLine createCommandLine() throws ExecutionException {
        return createGeneralDefaultCmdLine((AbstractRubyRunConfiguration) getEnvironment().getRunProfile());
    }

    public static ProcessHandler createOSProcessHandler(GeneralCommandLine cmd) throws ExecutionException {
        ProcessHandlerBuilderFactory builderFactory = Application.get().getInstance(ProcessHandlerBuilderFactory.class);

        ProcessHandlerBuilder builder = builderFactory.newBuilder(cmd);
        if (RApplicationSettings.getInstance().useConsoleColorMode) {
            builder.colored();
        }
        return builder.build();
    }

    protected GeneralCommandLine createGeneralDefaultCmdLine(@Nonnull final AbstractRubyRunConfiguration config) throws CantRunException {
        checkConfiguration(config);

        final RubyRuntime runtime = config.getRuntime();
        if (runtime == null) {
            throw new CantRunException(LocalizeValue.localizeTODO(RBundle.message("sdk.no.specified")));
        }

        final String workingDir = config.getWorkingDirectory().trim().length() != 0 ? config.getWorkingDirectory() : null;

        return Runner.createAndSetupCmdLine(null, workingDir, config.getEnvs(), config.isPassParentEnvs(), runtime.getExecutablePath(),
            runtime.getExecutableParameters().toArray(String[]::new));
    }

    protected void checkConfiguration(@Nonnull final AbstractRubyRunConfiguration config) throws CantRunException {
        try {
            config.checkConfiguration();
        }
        catch (RuntimeConfigurationException e) {
            throw new CantRunException(e.getMessage(), e);
        }
    }
}
