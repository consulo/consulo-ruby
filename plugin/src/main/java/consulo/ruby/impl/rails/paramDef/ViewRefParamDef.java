package consulo.ruby.impl.rails.paramDef;

import consulo.language.psi.PsiElement;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.rails.facet.configuration.StandardRailsPaths;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubyLookupItem;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.LookupItemType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamContext;

import java.util.Collection;

/**
 * A view template by its path under the views folder: {@code render template: 'users/show'}.
 */
public class ViewRefParamDef extends RailsParamDefBase {
    @Nullable
    @Override
    public Collection<RubyLookupItem> getVariants(ParamContext context) {
        VirtualFile root = getViewsRoot(context);
        return collectFiles(context, root, file -> createLookupItem(context, buildViewName(context, root, file), LookupItemType.String));
    }

    @Nullable
    @Override
    public PsiElement resolveReference(ParamContext context) {
        VirtualFile root = getViewsRoot(context);
        VirtualFile file = root != null ? findViewFile(context, root, getElementText(context)) : null;
        return findPsiFile(context, file);
    }

    @Nullable
    protected VirtualFile getViewsRoot(@Nonnull ParamContext context) {
        return findRoot(context, StandardRailsPaths::getViewsRootURL);
    }

    /**
     * @return the template a name stands for: any file of the folder named the name up to its extensions
     */
    @Nullable
    protected VirtualFile findViewFile(@Nonnull ParamContext context, @Nonnull VirtualFile root, @Nonnull String name) {
        int slash = name.lastIndexOf('/');
        VirtualFile folder = slash >= 0 ? root.findFileByRelativePath(name.substring(0, slash)) : root;
        return folder != null ? findTemplate(folder, name.substring(slash + 1)) : null;
    }

    @Nullable
    protected String buildViewName(@Nonnull ParamContext context, @Nonnull VirtualFile root, @Nonnull VirtualFile file) {
        String relative = VirtualFileUtil.getRelativePath(file.getParent(), root, '/');
        if (relative == null) {
            return null;
        }
        return relative.isEmpty() ? getBaseName(file) : relative + "/" + getBaseName(file);
    }

    @Nullable
    protected static VirtualFile findTemplate(@Nonnull VirtualFile folder, @Nonnull String baseName) {
        for (VirtualFile child : folder.getChildren()) {
            if (!child.isDirectory() && getBaseName(child).equals(baseName)) {
                return child;
            }
        }
        return null;
    }
}
