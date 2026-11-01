package consulo.ruby.impl.bundler.task;

import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.externalSystem.task.ExternalSystemTaskManager;
import consulo.process.ExecutionException;
import consulo.process.cmd.GeneralCommandLine;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ruby.impl.bundler.BundlerConstants;
import consulo.ruby.impl.bundler.BundlerPaths;
import consulo.ruby.impl.bundler.BundlerProcessRunner;
import consulo.ruby.impl.bundler.BundlerRubyLocator;
import consulo.ruby.impl.bundler.setting.BundlerExecutionSettings;
import jakarta.annotation.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs {@code bundle install} / {@code bundle update}, and every other task as a rake task with the gems of the Gemfile.
 *
 * @author VISTALL
 * @since 2026-11-01
 */
public class BundlerTaskManager implements ExternalSystemTaskManager<BundlerExecutionSettings> {
    private final BundlerProcessRunner myRunner = new BundlerProcessRunner();

    @Override
    public void executeTasks(ExternalSystemTaskId id,
                             List<String> taskNames,
                             String projectPath,
                             @Nullable BundlerExecutionSettings settings,
                             List<String> vmOptions,
                             List<String> scriptParameters,
                             @Nullable String debuggerSetup,
                             ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        Path rootDir = BundlerPaths.getRootDir(projectPath);
        String rubyExecutable = settings != null ? settings.getRubyExecutable() : BundlerRubyLocator.PATH_RUBY;

        for (String taskName : taskNames) {
            List<String> parameters = new ArrayList<>(vmOptions);
            if (BundlerConstants.TASK_INSTALL.equals(taskName)) {
                parameters.addAll(BundlerProcessRunner.gemExecutable("bundler", "bundle"));
                parameters.add("install");
            }
            else if (BundlerConstants.TASK_UPDATE.equals(taskName)) {
                parameters.addAll(BundlerProcessRunner.gemExecutable("bundler", "bundle"));
                parameters.add("update");
            }
            else {
                parameters.addAll(BundlerProcessRunner.bundledRake());
                parameters.add(taskName);
            }
            parameters.addAll(scriptParameters);

            run(id, BundlerProcessRunner.createCommandLine(rubyExecutable, rootDir, parameters), listener);
        }
    }

    private void run(ExternalSystemTaskId id, GeneralCommandLine commandLine, ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        String commandLineString = commandLine.getCommandLineString();
        listener.onTaskOutput(id, commandLineString + "\n", true);
        int exitCode;
        try {
            exitCode = myRunner.run(commandLine, id, listener);
        }
        catch (ExecutionException e) {
            throw new ExternalSystemException(e.getMessage());
        }
        if (exitCode == BundlerProcessRunner.CANCELLED_EXIT_CODE) {
            throw new ExternalSystemException(RubyApiLocalize.bundlerErrorCancelled().get());
        }
        if (exitCode != 0) {
            throw new ExternalSystemException(RubyApiLocalize.bundlerErrorExitCode(commandLineString, exitCode).get());
        }
    }

    @Override
    public boolean cancelTask(ExternalSystemTaskId id, ExternalSystemTaskNotificationListener listener) throws ExternalSystemException {
        myRunner.cancel();
        return true;
    }
}
