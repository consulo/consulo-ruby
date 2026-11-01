package consulo.jruby.impl.intention;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.localize.LocalizeValue;
import consulo.ruby.api.localize.RubyApiLocalize;
import jakarta.annotation.Nonnull;

/**
 * {@code java.util.List} to {@code List} plus {@code include_class 'java.util.List'}.
 */
@ExtensionImpl
@IntentionMetaData(ignoreId = "jruby.QualifiedNameToIncludeClassIntention", categories = "Ruby", fileExtensions = "rb")
public class QualifiedNameToIncludeClassIntention extends QualifiedNameToJavaImportIntentionBase {
    @Nonnull
    @Override
    public LocalizeValue getText() {
        return RubyApiLocalize.rubyIntentionsQualifiedNameToInclude_class();
    }

    @Nonnull
    @Override
    protected String createImportStatement(@Nonnull String qualifiedName) {
        return "include_class '" + qualifiedName + "'";
    }
}
