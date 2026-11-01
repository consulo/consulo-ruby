package consulo.jruby.impl.intention;

import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.search.PsiShortNamesCache;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.codeEditor.EditorPopupHelper;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.intention.IntentionAction;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.psi.PsiFile;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.popup.BaseListPopupStep;
import consulo.ui.ex.popup.JBPopupFactory;
import consulo.ui.ex.popup.PopupStep;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.methodCall.RCallNavigator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.references.RReferenceNavigator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RConstant;

import java.util.List;

/**
 * Replaces a constant with the fully qualified name of the Java class it names: {@code ArrayList} to {@code java.util.ArrayList}.
 */
@ExtensionImpl
@IntentionMetaData(ignoreId = "jruby.JavaFullClassNameIntention", categories = "Ruby", fileExtensions = "rb")
public class JavaFullClassNameIntention implements IntentionAction {
    @Nonnull
    @Override
    public LocalizeValue getText() {
        return RubyApiLocalize.rubyIntentionsJavaFullClassName();
    }

    @Override
    @RequiredReadAction
    public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
        if (editor == null || !(file instanceof RFile rFile) || !rFile.isInteropEnabled()) {
            return false;
        }
        RConstant constant = findConstant(editor, file);
        return constant != null && findClasses(project, constant).length > 0;
    }

    @Override
    @RequiredUIAccess
    public void invoke(@Nonnull Project project, Editor editor, PsiFile file) {
        RConstant constant = findConstant(editor, file);
        if (constant == null) {
            return;
        }
        PsiClass[] classes = findClasses(project, constant);
        if (classes.length == 1) {
            replace(project, file, constant, classes[0]);
        }
        else if (classes.length > 1) {
            BaseListPopupStep<PsiClass> step = new BaseListPopupStep<>(RubyApiLocalize.classToImportChooserTitle().get(), List.of(classes)) {
                @Nonnull
                @Override
                public String getTextFor(PsiClass value) {
                    String qualifiedName = value.getQualifiedName();
                    return qualifiedName != null ? qualifiedName : String.valueOf(value.getName());
                }

                @Override
                public PopupStep onChosen(PsiClass selectedValue, boolean finalChoice) {
                    return doFinalStep(() -> replace(project, file, constant, selectedValue));
                }
            };
            EditorPopupHelper.getInstance().showPopupInBestPositionFor(editor, JBPopupFactory.getInstance().createListPopup(step));
        }
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @RequiredUIAccess
    private static void replace(@Nonnull Project project, @Nonnull PsiFile file, @Nonnull RConstant constant, @Nonnull PsiClass psiClass) {
        String qualifiedName = psiClass.getQualifiedName();
        if (qualifiedName == null || !constant.isValid()) {
            return;
        }
        WriteCommandAction.runWriteCommandAction(project, RubyApiLocalize.rubyIntentionsJavaFullClassName().get(), null, () -> {
            List<RPsiElement> elements = RubyPsiUtil.getTopLevelElements(project, qualifiedName);
            if (!elements.isEmpty()) {
                RubyPsiUtil.replaceInParent(constant, elements.get(0));
            }
        }, file);
    }

    @Nullable
    private static RConstant findConstant(@Nonnull Editor editor, @Nonnull PsiFile file) {
        RConstant constant = PsiTreeUtil.getParentOfType(file.findElementAt(editor.getCaretModel().getOffset()), RConstant.class);
        if (constant == null || RReferenceNavigator.getReferenceByRightPart(constant) != null || RCallNavigator.getByCommand((RPsiElement) constant) != null) {
            return null;
        }
        return constant;
    }

    @Nonnull
    private static PsiClass[] findClasses(@Nonnull Project project, @Nonnull RConstant constant) {
        return PsiShortNamesCache.getInstance(project).getClassesByName(constant.getText(), GlobalSearchScope.allScope(project));
    }
}
