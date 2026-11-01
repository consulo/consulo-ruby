package consulo.ruby.impl.debugger;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.execution.debug.breakpoint.XLineBreakpointType;
import consulo.execution.debug.breakpoint.XLineBreakpointTypeResolver;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType;
import org.jspecify.annotations.Nullable;

@ExtensionImpl
public class RubyLineBreakpointTypeResolver implements XLineBreakpointTypeResolver {
    @RequiredReadAction
    @Override
    public @Nullable XLineBreakpointType<?> resolveBreakpointType(Project project, VirtualFile virtualFile, int line) {
        return RubyLineBreakpointType.getInstance();
    }

    @Override
    public FileType getFileType() {
        return RubyFileType.INSTANCE;
    }
}
