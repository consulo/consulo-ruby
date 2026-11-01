package consulo.ruby.impl.debugger;

import consulo.execution.debug.breakpoint.XBreakpointProperties;

public class RubyLineBreakpointProperties extends XBreakpointProperties<RubyLineBreakpointProperties> {
    @Override
    public RubyLineBreakpointProperties getState() {
        return this;
    }

    @Override
    public void loadState(RubyLineBreakpointProperties state) {
    }
}
