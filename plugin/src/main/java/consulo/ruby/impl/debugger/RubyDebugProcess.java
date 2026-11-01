package consulo.ruby.impl.debugger;

import consulo.execution.ExecutionResult;
import consulo.execution.debug.XDebugSession;
import consulo.execution.debug.breakpoint.XLineBreakpointType;
import consulo.execution.debug.evaluation.XDebuggerEditorsProvider;
import consulo.execution.debugger.dap.DAPDebugProcess;
import consulo.execution.debugger.dap.protocol.DAP;
import consulo.execution.debugger.dap.protocol.DAPFactory;
import consulo.execution.debugger.dap.protocol.event.ExitedEvent;
import consulo.execution.debugger.dap.protocol.event.OutputEvent;
import consulo.execution.ui.ExecutionConsole;
import consulo.process.ProcessHandler;
import jakarta.annotation.Nonnull;
import org.jspecify.annotations.Nullable;

/**
 * Debugs a Ruby process through the DAP server of the debug gem, which the process opens on a local port.
 */
public class RubyDebugProcess extends DAPDebugProcess {
    public static final String HOST = "127.0.0.1";

    private static final String REPL_GREETING = "Ruby REPL:";

    private final ExecutionResult myExecutionResult;
    private final int myPort;

    public RubyDebugProcess(XDebugSession session, ExecutionResult executionResult, int port) {
        super(session);
        myExecutionResult = executionResult;
        myPort = port;
    }

    @Nonnull
    @Override
    protected XLineBreakpointType<?> getLineBreakpointType() {
        return RubyLineBreakpointType.getInstance();
    }

    @Override
    protected DAP createDAP(DAPFactory factory) {
        // the connection retries until the debug gem listens
        return factory.createSocketDAP(HOST, myPort);
    }

    @Override
    protected String getAdapterId() {
        return "rdbg";
    }

    @Override
    public XDebuggerEditorsProvider getEditorsProvider() {
        return RubyDebuggerEditorsProvider.INSTANCE;
    }

    @Override
    protected @Nullable ProcessHandler doGetProcessHandler() {
        return myExecutionResult.getProcessHandler();
    }

    @Override
    public ExecutionConsole createConsole() {
        return myExecutionResult.getExecutionConsole();
    }

    @Override
    protected void onOutput(OutputEvent event) {
        // the greeting of the debug gem's REPL - the debug console here evaluates through the IDE
        if (event.output != null && event.output.startsWith(REPL_GREETING)) {
            return;
        }
        super.onOutput(event);
    }

    @Override
    protected void onExited(ExitedEvent event) {
        // the process handler reports the exit code
    }

    @Override
    protected void stopImpl() {
        super.stopImpl();
        ProcessHandler processHandler = myExecutionResult.getProcessHandler();
        if (!processHandler.isProcessTerminated()) {
            processHandler.destroyProcess();
        }
    }
}
