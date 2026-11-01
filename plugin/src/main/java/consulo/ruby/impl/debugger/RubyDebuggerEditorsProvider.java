package consulo.ruby.impl.debugger;

import consulo.execution.debug.evaluation.XDebuggerEditorsProviderBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.project.Project;
import consulo.virtualFileSystem.fileType.FileType;
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType;
import org.jspecify.annotations.Nullable;

/**
 * Ruby editors for watches, Evaluate Expression and breakpoint conditions.
 */
public class RubyDebuggerEditorsProvider extends XDebuggerEditorsProviderBase {
    public static final RubyDebuggerEditorsProvider INSTANCE = new RubyDebuggerEditorsProvider();

    @Override
    public FileType getFileType() {
        return RubyFileType.INSTANCE;
    }

    @Override
    protected PsiFile createExpressionCodeFragment(Project project, String text, @Nullable PsiElement context, boolean isPhysical) {
        return PsiFileFactory.getInstance(project).createFileFromText("fragment.rb", RubyFileType.INSTANCE, text, System.currentTimeMillis(), isPhysical);
    }
}
