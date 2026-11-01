package consulo.jruby.impl.intention;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.localize.LocalizeValue;
import consulo.ruby.api.localize.RubyApiLocalize;
import jakarta.annotation.Nonnull;

/**
 * {@code java.util.List} to {@code List} plus {@code java_import java.util.List}.
 */
@ExtensionImpl
@IntentionMetaData(ignoreId = "jruby.QualifiedNameToImportClassIntention", categories = "Ruby", fileExtensions = "rb")
public class QualifiedNameToImportClassIntention extends QualifiedNameToJavaImportIntentionBase {
    @Nonnull
    @Override
    public LocalizeValue getText() {
        return RubyApiLocalize.rubyIntentionsQualifiedNameToImport();
    }

    @Nonnull
    @Override
    protected String createImportStatement(@Nonnull String qualifiedName) {
        return "java_import " + qualifiedName;
    }
}
