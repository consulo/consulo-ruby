package consulo.ruby.impl.bundler;

import consulo.application.Application;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilderFactory;
import consulo.process.ProcessOutputType;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.event.ProcessEvent;
import consulo.process.event.ProcessListener;
import consulo.util.dataholder.Key;
import jakarta.annotation.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
public final class BundlerProcessRunner {
    public static final int CANCELLED_EXIT_CODE = Integer.MIN_VALUE;
    public static final int TIMEOUT_EXIT_CODE = Integer.MIN_VALUE + 1;

    private static final long WAIT_INTERVAL_MS = 100;

    @Nullable
    private volatile ProcessHandler myHandler;
    private volatile boolean myCancelled;

    public static GeneralCommandLine createCommandLine(String rubyExecutable, Path rootDir, List<String> parameters) {
        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.withExePath(rubyExecutable);
        commandLine.withWorkingDirectory(rootDir);
        commandLine.withEnvironment("BUNDLE_GEMFILE", rootDir.resolve(BundlerConstants.GEMFILE).toString());
        commandLine.withParameters(parameters);
        return commandLine;
    }

    /**
     * Runs an executable of a gem through the interpreter, so the gem directories of the interpreter decide and not the PATH.
     */
    public static List<String> gemExecutable(String gemName, String executable) {
        return List.of("-e", "load Gem.bin_path('" + gemName + "', '" + executable + "')", "--");
    }

    /**
     * {@code rake} with the gems of the Gemfile, as {@code bundle exec rake} does.
     */
    public static List<String> bundledRake() {
        return List.of("-rbundler/setup", "-e", "load Gem.bin_path('rake', 'rake')", "--");
    }

    public int run(GeneralCommandLine commandLine, ExternalSystemTaskId id, ExternalSystemTaskNotificationListener listener) throws ExecutionException {
        return run(commandLine, (text, stdout) -> listener.onTaskOutput(id, text, stdout), 0);
    }

    /**
     * @param timeoutMs 0 waits until the process ends
     */
    public int run(GeneralCommandLine commandLine, BiConsumer<String, Boolean> output, long timeoutMs) throws ExecutionException {
        if (myCancelled) {
            return CANCELLED_EXIT_CODE;
        }

        ProcessHandler handler = Application.get().getInstance(ProcessHandlerBuilderFactory.class).newBuilder(commandLine).build();
        handler.addProcessListener(new ProcessListener() {
            @Override
            public void onTextAvailable(ProcessEvent event, Key outputType) {
                if (outputType != ProcessOutputType.SYSTEM) {
                    output.accept(event.getText(), outputType != ProcessOutputType.STDERR);
                }
            }
        });

        long deadline = timeoutMs > 0 ? System.currentTimeMillis() + timeoutMs : Long.MAX_VALUE;
        myHandler = handler;
        try {
            handler.startNotify();
            while (!handler.waitFor(WAIT_INTERVAL_MS)) {
                boolean timedOut = System.currentTimeMillis() > deadline;
                if (myCancelled || timedOut) {
                    handler.destroyProcess();
                    handler.waitFor();
                    return timedOut ? TIMEOUT_EXIT_CODE : CANCELLED_EXIT_CODE;
                }
            }
        }
        finally {
            myHandler = null;
        }
        Integer exitCode = handler.getExitCode();
        return exitCode == null ? -1 : exitCode;
    }

    public void cancel() {
        myCancelled = true;
        ProcessHandler handler = myHandler;
        if (handler != null) {
            handler.destroyProcess();
        }
    }
}
