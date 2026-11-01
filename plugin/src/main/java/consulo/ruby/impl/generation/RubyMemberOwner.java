package consulo.ruby.impl.generation;

import consulo.language.editor.generation.MemberChooserObjectBase;
import consulo.ui.image.Image;
import jakarta.annotation.Nullable;

import java.util.Objects;

/**
 * The class or module grouping methods in the member chooser.
 */
public class RubyMemberOwner extends MemberChooserObjectBase {
    public RubyMemberOwner(String text, @Nullable Image icon) {
        super(text, icon);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return o instanceof RubyMemberOwner owner && Objects.equals(getText(), owner.getText());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getText());
    }
}
