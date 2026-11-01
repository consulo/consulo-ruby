package consulo.ruby.impl.refactoring;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.refactoring.RefactoringSupportProvider;
import consulo.language.editor.refactoring.action.RefactoringActionHandler;
import consulo.ruby.impl.refactoring.introduceVariable.RubyIntroduceVariableHandler;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.lang.RubyLanguage;

@ExtensionImpl
public class RubyRefactoringSupportProvider extends RefactoringSupportProvider {
    @Nullable
    @Override
    public RefactoringActionHandler getIntroduceVariableHandler() {
        return new RubyIntroduceVariableHandler();
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return RubyLanguage.INSTANCE;
    }
}
