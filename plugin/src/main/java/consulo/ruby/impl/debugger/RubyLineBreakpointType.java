package consulo.ruby.impl.debugger;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.execution.debug.breakpoint.XBreakpointType;
import consulo.execution.debug.breakpoint.XLineBreakpoint;
import consulo.execution.debug.breakpoint.XLineBreakpointType;
import consulo.execution.debug.evaluation.XDebuggerEditorsProvider;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;
import org.jspecify.annotations.Nullable;

/**
 * A line breakpoint in Ruby code, set through the debug gem.
 */
@ExtensionImpl
public class RubyLineBreakpointType extends XLineBreakpointType<RubyLineBreakpointProperties> {
    public static RubyLineBreakpointType getInstance() {
        return Application.get().getExtensionPoint(XBreakpointType.class).findExtensionOrFail(RubyLineBreakpointType.class);
    }

    @Inject
    public RubyLineBreakpointType() {
        super("ruby-line", "Ruby Line Breakpoint");
    }

    @Override
    public @Nullable RubyLineBreakpointProperties createBreakpointProperties(VirtualFile file, int line) {
        return new RubyLineBreakpointProperties();
    }

    @Override
    public @Nullable XDebuggerEditorsProvider getEditorsProvider(XLineBreakpoint<RubyLineBreakpointProperties> breakpoint, Project project) {
        return RubyDebuggerEditorsProvider.INSTANCE;
    }
}
