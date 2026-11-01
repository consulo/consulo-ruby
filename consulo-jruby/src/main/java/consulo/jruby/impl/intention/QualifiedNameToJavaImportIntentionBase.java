package consulo.jruby.impl.intention;

import com.intellij.java.language.psi.PsiClass;
import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.Editor;
import consulo.language.editor.intention.IntentionAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.ResolveUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.ForeignSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;

import java.util.List;

/**
 * Replaces a qualified Java class name with its short name and adds the statement importing the class.
 */
public abstract class QualifiedNameToJavaImportIntentionBase implements IntentionAction {
    /**
     * @return the statement importing the class, e.g. {@code java_import java.util.List}
     */
    @Nonnull
    protected abstract String createImportStatement(@Nonnull String qualifiedName);

    @Override
    @RequiredReadAction
    public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
        return editor != null && file instanceof RFile rFile && rFile.isInteropEnabled() && findClassSymbol(findReference(editor, file)) != null;
    }

    @Override
    public void invoke(@Nonnull Project project, Editor editor, PsiFile file) {
        RPsiElement reference = findReference(editor, file);
        ForeignSymbol symbol = findClassSymbol(reference);
        String qualifiedName = symbol != null && symbol.getPsiElement() instanceof PsiClass psiClass ? psiClass.getQualifiedName() : null;
        if (reference == null || qualifiedName == null) {
            return;
        }
        List<RPsiElement> constants = RubyPsiUtil.getTopLevelElements(project, symbol.getName());
        List<RPsiElement> imports = RubyPsiUtil.getTopLevelElements(project, createImportStatement(qualifiedName));
        if (constants.isEmpty() || imports.isEmpty()) {
            return;
        }
        RPsiElement constant = constants.get(0);
        RubyPsiUtil.replaceInParent(reference, constant);
        RPsiElement statement = RubyPsiUtil.getStatement(constant);
        if (statement != null) {
            RubyPsiUtil.addBeforeInParent(statement, imports.get(0), RubyPsiUtil.createNewLine(project));
        }
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    @Nullable
    private static RPsiElement findReference(@Nonnull Editor editor, @Nonnull PsiFile file) {
        RPsiElement element = PsiTreeUtil.getParentOfType(file.findElementAt(editor.getCaretModel().getOffset()), RPsiElement.class);
        if (element != null && element.getReference() == null && element.getParent() instanceof RPsiElement parent) {
            return parent;
        }
        return element;
    }

    @Nullable
    @RequiredReadAction
    private static ForeignSymbol findClassSymbol(@Nullable PsiElement reference) {
        if (reference == null) {
            return null;
        }
        List<Symbol> symbols = ResolveUtil.resolveToSymbols(reference);
        return symbols.size() == 1 && symbols.get(0).getType() == Type.FOREIGN_CLASS ? (ForeignSymbol) symbols.get(0) : null;
    }
}
