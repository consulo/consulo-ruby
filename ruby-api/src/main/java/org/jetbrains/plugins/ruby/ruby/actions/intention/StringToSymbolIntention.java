package org.jetbrains.plugins.ruby.ruby.actions.intention;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.editor.intention.IntentionAction;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ruby.api.localize.RubyApiLocalize;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.basicTypes.stringLiterals.RBaseString;

import java.util.List;

/**
 * Converts a string literal holding an identifier into a symbol: {@code "name"} to {@code :name}.
 */
@ExtensionImpl
@IntentionMetaData(ignoreId = "ruby.StringToSymbolIntention", categories = "Ruby", fileExtensions = "rb")
public class StringToSymbolIntention implements IntentionAction {
    @Nonnull
    @Override
    public LocalizeValue getText() {
        return RubyApiLocalize.rubyIntentionsStringToSymbol();
    }

    @Override
    @RequiredReadAction
    public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
        if (editor == null || !RubyIntentionUtil.isAvailable(editor, file)) {
            return false;
        }
        RBaseString string = findString(editor, file);
        return string != null && !string.hasExpressionSubstitutions() && TextUtil.isCID(string.getContent());
    }

    @Override
    public void invoke(@Nonnull Project project, Editor editor, PsiFile file) {
        RBaseString string = findString(editor, file);
        if (string == null) {
            return;
        }
        List<RPsiElement> elements = RubyPsiUtil.getTopLevelElements(project, ":" + string.getContent());
        if (!elements.isEmpty()) {
            RubyPsiUtil.replaceInParent(string, elements.get(0));
        }
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    @Nullable
    private static RBaseString findString(@Nonnull Editor editor, @Nonnull PsiFile file) {
        return PsiTreeUtil.getParentOfType(file.findElementAt(editor.getCaretModel().getOffset()), RBaseString.class);
    }
}
