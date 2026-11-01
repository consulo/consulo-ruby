package consulo.ruby.impl.rails.paramDef;

import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamContext;

/**
 * A layout under {@code app/views/layouts}: {@code layout 'admin'}.
 */
public class LayoutRefParamDef extends ViewRefParamDef {
    @Nullable
    @Override
    protected VirtualFile getViewsRoot(@Nonnull ParamContext context) {
        VirtualFile views = super.getViewsRoot(context);
        return views != null ? views.findChild("layouts") : null;
    }
}
