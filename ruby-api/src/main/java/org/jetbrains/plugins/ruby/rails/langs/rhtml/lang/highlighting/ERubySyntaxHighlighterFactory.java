package org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.highlighting;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.highlight.SingleLazyInstanceSyntaxHighlighterFactory;
import consulo.language.editor.highlight.SyntaxHighlighter;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.eRubyLanguage;
import org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.highlighting.impl.RHTMLFileHighlighterImpl;

/**
 * The base layer of the ERB editor highlighter: the template tags; Ruby and the template data language highlight their parts.
 */
@ExtensionImpl
public class ERubySyntaxHighlighterFactory extends SingleLazyInstanceSyntaxHighlighterFactory {
    @Nonnull
    @Override
    protected SyntaxHighlighter createHighlighter() {
        return new RHTMLFileHighlighterImpl();
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return eRubyLanguage.INSTANCE;
    }
}
