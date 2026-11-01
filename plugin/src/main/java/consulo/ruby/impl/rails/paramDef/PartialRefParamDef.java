package consulo.ruby.impl.rails.paramDef;

import consulo.module.Module;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.rails.nameConventions.ViewsConventions;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamContext;

/**
 * A partial: {@code render partial: 'form'} is {@code _form.*} of the current views folder, {@code 'shared/menu'} is
 * {@code shared/_menu.*} under the views root.
 */
public class PartialRefParamDef extends ViewRefParamDef {
    @Nullable
    @Override
    protected VirtualFile findViewFile(@Nonnull ParamContext context, @Nonnull VirtualFile root, @Nonnull String name) {
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            String folderPath = name.substring(0, slash);
            VirtualFile folder = root.findFileByRelativePath(folderPath.startsWith("/") ? folderPath.substring(1) : folderPath);
            return folder != null ? findTemplate(folder, "_" + name.substring(slash + 1)) : null;
        }
        VirtualFile folder = getContextFolder(context, root);
        return folder != null ? findTemplate(folder, "_" + name) : null;
    }

    @Nullable
    @Override
    protected String buildViewName(@Nonnull ParamContext context, @Nonnull VirtualFile root, @Nonnull VirtualFile file) {
        if (!file.getName().startsWith("_")) {
            return null;
        }
        String name = getBaseName(file).substring(1);
        if (file.getParent().equals(getContextFolder(context, root))) {
            return name;
        }
        String relative = VirtualFileUtil.getRelativePath(file.getParent(), root, '/');
        return relative == null ? null : relative.isEmpty() ? name : relative + "/" + name;
    }

    /**
     * @return the views folder of the template or controller holding the call
     */
    @Nullable
    private static VirtualFile getContextFolder(@Nonnull ParamContext context, @Nonnull VirtualFile root) {
        VirtualFile file = context.getValueElement().getContainingFile().getVirtualFile();
        Module module = context.getModule();
        if (file == null || module == null) {
            return null;
        }
        return VirtualFileUtil.isAncestor(root, file, false) ? file.getParent() : ViewsConventions.getViewsFolder(file, module);
    }
}
