package consulo.ruby.impl.bundler.task;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.execution.icon.ExecutionIconGroup;
import consulo.execution.lineMarker.ExecutorAction;
import consulo.execution.lineMarker.RunLineMarkerContributor;
import consulo.language.Language;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import org.jetbrains.plugins.ruby.ruby.lang.RubyLanguage;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import org.jspecify.annotations.Nullable;

/**
 * Run icons for the tasks of Rakefiles.
 */
@ExtensionImpl
public class RakeTaskRunLineMarkerContributor extends RunLineMarkerContributor {
    @RequiredReadAction
    @Override
    public @Nullable Info getInfo(PsiElement element) {
        if (element.getFirstChild() != null) {
            return null;
        }
        RCall call = PsiTreeUtil.getParentOfType(element, RCall.class);
        if (call == null || PsiTreeUtil.getDeepestFirst(call) != element || RakeTaskUtil.findTaskCall(element) != call) {
            return null;
        }
        String taskName = RakeTaskUtil.getTaskName(call);
        return taskName == null ? null : new Info(ExecutionIconGroup.gutterRun(), e -> "Run rake " + taskName, ExecutorAction.getActions(0));
    }

    @Override
    public Language getLanguage() {
        return RubyLanguage.INSTANCE;
    }
}
