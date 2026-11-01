package consulo.ruby.impl.bundler.task;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.iterators.RBlockCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rake tasks declared in Rakefiles: <code>task :name</code>, <code>task name: :dependency</code>, in namespaces.
 */
public final class RakeTaskUtil {
    private static final String TASK = "task";
    private static final String NAMESPACE = "namespace";
    /**
     * The name of the first argument: <code>:name</code>, <code>"name"</code>, <code>name: ...</code>, <code>:name => ...</code>
     */
    private static final Pattern NAME = Pattern.compile("^:?[\"']?([\\w:.\\-]+?)[\"']?\\s*(?::(?!:)|=>|,|$).*", Pattern.DOTALL);

    private RakeTaskUtil() {
    }

    public static boolean isRakeFile(@Nullable VirtualFile file) {
        if (file == null) {
            return false;
        }
        String name = file.getName();
        return "Rakefile".equals(name) || "rakefile".equals(name) || "rake".equals(file.getExtension());
    }

    /**
     * @return the task call around the element
     */
    @RequiredReadAction
    public static @Nullable RCall findTaskCall(PsiElement element) {
        PsiFile file = element.getContainingFile();
        if (file == null || !isRakeFile(file.getVirtualFile())) {
            return null;
        }
        RCall call = PsiTreeUtil.getParentOfType(element, RCall.class, false);
        while (call != null && !isTaskCall(call)) {
            call = PsiTreeUtil.getParentOfType(call, RCall.class, true);
        }
        return call;
    }

    @RequiredReadAction
    public static boolean isTaskCall(RCall call) {
        return TASK.equals(call.getCommand()) && getArgumentName(call) != null;
    }

    /**
     * @return the full name of the task, with its namespaces: <code>db:seed</code>
     */
    @RequiredReadAction
    public static @Nullable String getTaskName(RCall taskCall) {
        String name = getArgumentName(taskCall);
        if (name == null) {
            return null;
        }
        StringBuilder fullName = new StringBuilder(name);
        RBlockCall blockCall = PsiTreeUtil.getParentOfType(taskCall, RBlockCall.class, true);
        while (blockCall != null) {
            if (blockCall.getCall() instanceof RCall call && NAMESPACE.equals(call.getCommand())) {
                String namespace = getArgumentName(call);
                if (namespace != null) {
                    fullName.insert(0, namespace + ":");
                }
            }
            blockCall = PsiTreeUtil.getParentOfType(blockCall, RBlockCall.class, true);
        }
        return fullName.toString();
    }

    @RequiredReadAction
    private static @Nullable String getArgumentName(RCall call) {
        List<RPsiElement> arguments = call.getArguments();
        if (arguments.isEmpty()) {
            return null;
        }
        Matcher matcher = NAME.matcher(arguments.get(0).getText().trim());
        return matcher.matches() ? matcher.group(1) : null;
    }
}
