package consulo.ruby.impl.debugger;

import consulo.annotation.component.ExtensionImpl;
import consulo.container.plugin.PluginManager;
import consulo.document.FileDocumentManager;
import consulo.execution.ExecutionResult;
import consulo.execution.configuration.RunProfile;
import consulo.execution.configuration.RunProfileState;
import consulo.execution.debug.DefaultDebugExecutor;
import consulo.execution.debug.XDebugProcess;
import consulo.execution.debug.XDebugProcessStarter;
import consulo.execution.debug.XDebugSession;
import consulo.execution.debug.XDebuggerManager;
import consulo.execution.runner.DefaultProgramRunner;
import consulo.execution.runner.ExecutionEnvironment;
import consulo.execution.ui.RunContentDescriptor;
import consulo.process.ExecutionException;
import consulo.util.io.NetUtil;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.AbstractRubyRunConfiguration;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.ColouredCommandLineState;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkType;
import consulo.ruby.runtime.RubyRuntime;

import java.io.File;
import java.io.IOException;

/**
 * Debugs Ruby scripts and tests: the program loads the debug gem's DAP server and waits for the IDE to connect.
 */
@ExtensionImpl
public class RubyDebugRunner extends DefaultProgramRunner {
    private static final String LOADER = "debugger/consulo_debug_loader.rb";

    @Nonnull
    @Override
    public String getRunnerId() {
        return "RubyDebug";
    }

    @Override
    public boolean canRun(@Nonnull String executorId, @Nonnull RunProfile profile) {
        if (!DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) || !(profile instanceof AbstractRubyRunConfiguration configuration)) {
            return false;
        }
        RubyRuntime runtime = configuration.getRuntime();
        return runtime != null && runtime.isDebuggable();
    }

    @Override
    protected RunContentDescriptor doExecute(@Nonnull RunProfileState state, @Nonnull ExecutionEnvironment env) throws ExecutionException {
        if (!(state instanceof ColouredCommandLineState rubyState)) {
            throw new ExecutionException("Can't debug " + env.getRunProfile().getName());
        }

        FileDocumentManager.getInstance().saveAllDocuments();

        int port;
        try {
            port = NetUtil.findAvailableSocketPort();
        }
        catch (IOException e) {
            throw new ExecutionException("No free port for the debugger: " + e.getMessage(), e);
        }

        rubyState.addEnvironment("RUBY_DEBUG_HOST", RubyDebugProcess.HOST);
        rubyState.addEnvironment("RUBY_DEBUG_PORT", String.valueOf(port));
        // no "wait for debugger connection" and similar status lines in the program output
        rubyState.addEnvironment("RUBY_DEBUG_LOG_LEVEL", "ERROR");
        rubyState.addRequiredLibrary(new File(PluginManager.getPluginPath(RubySdkType.class), LOADER).getPath());

        ExecutionResult result = rubyState.execute(env.getExecutor(), this);

        XDebugSession session = XDebuggerManager.getInstance(env.getProject()).startSession(env, new XDebugProcessStarter() {
            @Nonnull
            @Override
            public XDebugProcess start(@Nonnull XDebugSession session) {
                RubyDebugProcess process = new RubyDebugProcess(session, result, port);
                process.start();
                return process;
            }
        });
        return session.getRunContentDescriptor();
    }
}
