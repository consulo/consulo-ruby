package consulo.ruby.impl.generation;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.generation.OverrideMethodHandler;
import consulo.localize.LocalizeValue;
import consulo.ruby.api.localize.RubyApiLocalize;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.RubyOverrideImplementUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;

import java.util.List;


/**
 * Code | Override Methods: the methods of the superclasses and included modules the class does not define yet.
 */
@ExtensionImpl
public class RubyOverrideMethodHandler extends RubyGenerateMethodsHandlerBase implements OverrideMethodHandler {
    @Nonnull
    @Override
    protected LocalizeValue getTitle() {
        return RubyApiLocalize.refactoringOverrideMethodsTitle();
    }

    @Nonnull
    @Override
    @RequiredReadAction
    protected List<Symbol> collectMethods(@Nullable FileSymbol fileSymbol, @Nonnull Symbol classSymbol) {
        return RubyOverrideImplementUtil.getMethodsToOverride(fileSymbol, classSymbol);
    }
}
