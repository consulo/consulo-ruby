package consulo.ruby.impl.generation;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.language.editor.generation.ImplementMethodHandler;
import consulo.language.psi.PsiFile;
import consulo.localize.LocalizeValue;
import consulo.ruby.api.localize.RubyApiLocalize;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.RubyOverrideImplementUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;

import java.util.List;


/**
 * Code | Implement Methods: the abstract foreign methods (e.g. of a Java interface a JRuby class includes) the class does not define.
 */
@ExtensionImpl
public class RubyImplementMethodHandler extends RubyGenerateMethodsHandlerBase implements ImplementMethodHandler {
    @Nonnull
    @Override
    protected LocalizeValue getTitle() {
        return RubyApiLocalize.refactoringImplementJavaMethodsTitle();
    }

    @Override
    @RequiredReadAction
    public boolean isValidFor(Editor editor, PsiFile file) {
        Symbol classSymbol = findClassSymbol(editor, file);
        return classSymbol != null && !collectMethods(getFileSymbol(file.getProject()), classSymbol).isEmpty();
    }

    @Nonnull
    @Override
    @RequiredReadAction
    protected List<Symbol> collectMethods(@Nullable FileSymbol fileSymbol, @Nonnull Symbol classSymbol) {
        return RubyOverrideImplementUtil.getMethodsToImplement(fileSymbol, classSymbol);
    }
}
