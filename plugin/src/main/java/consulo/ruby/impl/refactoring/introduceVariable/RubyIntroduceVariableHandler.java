package consulo.ruby.impl.refactoring.introduceVariable;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.Application;
import consulo.codeEditor.Editor;
import consulo.codeEditor.SelectionModel;
import consulo.dataContext.DataContext;
import consulo.document.Document;
import consulo.language.ast.ASTNode;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.refactoring.action.RefactoringActionHandler;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.dialog.DialogService;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.scope.PseudoScopeHolder;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.scope.ScopeHolder;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.scope.ScopeUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.scope.ScopeVariable;
import org.jetbrains.plugins.ruby.ruby.lang.namesValidator.RubyNamesValidator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.refactoring.RubyRefactoringUtil;
import org.jetbrains.plugins.ruby.ruby.refactoring.introduceVariable.NamesSuggestor;

import java.util.ArrayList;
import java.util.List;

/**
 * Refactor | Introduce Variable: assigns the selected expression (or the statement on the caret line) to a new local
 * variable and replaces the expression, or all its occurrences in the scope, with the variable.
 */
public class RubyIntroduceVariableHandler implements RefactoringActionHandler {
    @Override
    @RequiredUIAccess
    public void invoke(@Nonnull Project project, Editor editor, PsiFile file, DataContext dataContext) {
        RPsiElement expression = findSelectedElement(editor, file);
        RPsiElement scope = expression != null ? PsiTreeUtil.getParentOfType(expression, ScopeHolder.class) : null;
        if (expression == null || scope == null) {
            return;
        }
        List<PsiElement> occurrences = RubyRefactoringUtil.getOccurences(expression, scope);
        if (occurrences.isEmpty()) {
            occurrences = List.of(expression);
        }
        List<PsiElement> allOccurrences = occurrences;

        List<String> names = new ArrayList<>();
        for (String name : NamesSuggestor.getSuggestedNames(expression)) {
            if (checkName(project, name, allOccurrences).isEmpty()) {
                names.add(name);
            }
        }

        RubyIntroduceVariableDialogDescriptor descriptor =
            new RubyIntroduceVariableDialogDescriptor(allOccurrences.size(), names, name -> checkName(project, name, allOccurrences));
        Application.get().getInstance(DialogService.class).build(descriptor).showAsync().thenAccept(value -> {
            if (value instanceof RubyIntroduceVariableResult result) {
                WriteCommandAction.runWriteCommandAction(project, RubyApiLocalize.refactoringIntroduceVariableDialogTitle().get(), null,
                    () -> introduce(project, expression, allOccurrences, result.name(), result.replaceAll()), file);
            }
        });
    }

    @Override
    public void invoke(@Nonnull Project project, @Nonnull PsiElement[] elements, DataContext dataContext) {
    }

    /**
     * @return the error for the variable name, empty if it may be used
     */
    @Nonnull
    @RequiredReadAction
    private static LocalizeValue checkName(@Nonnull Project project, @Nonnull String name, @Nonnull List<PsiElement> occurrences) {
        RubyNamesValidator validator = new RubyNamesValidator();
        if (validator.isKeyword(name, project)) {
            return RubyApiLocalize.refactoringIntroduceVariableReswordError();
        }
        if (name.isEmpty() || !validator.isIdentifier(name, project) || !Character.isLowerCase(name.charAt(0)) && name.charAt(0) != '_') {
            return RubyApiLocalize.refactoringIntroduceVariableInvalidName();
        }
        for (PsiElement occurrence : occurrences) {
            for (ScopeVariable variable : ScopeUtil.gatherScopeVariables(occurrence)) {
                if (name.equals(variable.getName())) {
                    return RubyApiLocalize.refactoringIntroduceVariableScopeError();
                }
            }
        }
        return LocalizeValue.empty();
    }

    private static void introduce(@Nonnull Project project, @Nonnull RPsiElement expression, @Nonnull List<PsiElement> occurrences, @Nonnull String name, boolean replaceAll) {
        if (!expression.isValid()) {
            return;
        }
        List<PsiElement> toReplace = replaceAll ? occurrences : List.of(expression);
        RPsiElement anchor = findAnchorStatement(expression, toReplace);
        if (anchor == null) {
            return;
        }
        PsiElement assignment = RubyPsiUtil.createDummyRubyFile(project, name + " = " + expression.getText()).getCompoundStatement().getFirstChild();

        if (toReplace.size() == 1 && expression == RubyPsiUtil.getStatement(expression)) {
            RubyPsiUtil.replaceInParent(expression, assignment);
            return;
        }

        RubyPsiUtil.addBeforeInParent(anchor, assignment, RubyPsiUtil.createNewLine(project));
        ASTNode[] names = RubyPsiUtil.createDummyRubyFile(project, (name + "\n").repeat(toReplace.size())).getCompoundStatement().getNode().getChildren(null);
        for (int i = 0; i < toReplace.size(); i++) {
            RubyPsiUtil.replaceInParent(toReplace.get(i), names[i * 2].getPsi());
        }
    }

    /**
     * @return the statement the definition goes before: the one holding the first occurrence, in the scope common to all
     */
    @Nullable
    private static RPsiElement findAnchorStatement(@Nonnull RPsiElement expression, @Nonnull List<PsiElement> occurrences) {
        PsiElement anchor = expression;
        if (occurrences.size() > 1) {
            PsiElement parent = expression;
            for (PsiElement occurrence : occurrences) {
                if (!PsiTreeUtil.isAncestor(parent, occurrence, false)) {
                    parent = PsiTreeUtil.findCommonParent(parent, occurrence);
                }
            }
            PsiElement scope = parent instanceof PseudoScopeHolder ? parent : PsiTreeUtil.getParentOfType(parent, PseudoScopeHolder.class);
            for (PsiElement occurrence : occurrences) {
                PsiElement current = occurrence;
                PseudoScopeHolder holder;
                while ((holder = PsiTreeUtil.getParentOfType(current, PseudoScopeHolder.class)) != null && holder != scope) {
                    current = holder;
                }
                if (RubyPsiUtil.isBefore(current, anchor)) {
                    anchor = current;
                }
            }
        }
        return RubyPsiUtil.getStatement(anchor);
    }

    /**
     * @return the Ruby element covering the selection, or the statement on the caret line without a selection
     */
    @Nullable
    @RequiredReadAction
    private static RPsiElement findSelectedElement(@Nonnull Editor editor, @Nonnull PsiFile file) {
        SelectionModel selection = editor.getSelectionModel();
        PsiElement start;
        PsiElement end;
        if (selection.hasSelection()) {
            start = file.findElementAt(selection.getSelectionStart());
            end = file.findElementAt(selection.getSelectionEnd() - 1);
        }
        else {
            Document document = editor.getDocument();
            int line = document.getLineNumber(editor.getCaretModel().getOffset());
            if (line < 0 || line >= document.getLineCount()) {
                return null;
            }
            start = file.findElementAt(document.getLineStartOffset(line));
            end = file.findElementAt(Math.max(document.getLineStartOffset(line), document.getLineEndOffset(line) - 1));
        }
        if (start == null || end == null) {
            return null;
        }
        PsiElement parent = PsiTreeUtil.findCommonParent(start, end);
        return parent instanceof RPsiElement rPsiElement ? rPsiElement : PsiTreeUtil.getParentOfType(parent, RPsiElement.class);
    }
}
