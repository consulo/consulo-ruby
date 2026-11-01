package consulo.ruby.impl.rails.paramDef;

import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.module.Module;
import consulo.module.content.ProjectRootManager;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.rails.facet.RailsFacetUtil;
import org.jetbrains.plugins.ruby.rails.facet.configuration.StandardRailsPaths;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubyLookupItem;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.LookupItemType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamContext;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamDef;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamDefImplUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A parameter of a Rails method naming an application file: a view, a controller, an asset...
 */
public abstract class RailsParamDefBase extends ParamDef {
    @Nullable
    protected static StandardRailsPaths getPaths(@Nonnull ParamContext context) {
        Module module = context.getModule();
        return module != null ? RailsFacetUtil.getRailsAppPaths(module) : null;
    }

    @Nullable
    protected static VirtualFile findRoot(@Nonnull ParamContext context, @Nonnull Function<StandardRailsPaths, String> url) {
        StandardRailsPaths paths = getPaths(context);
        String rootUrl = paths != null ? url.apply(paths) : null;
        return rootUrl != null ? VirtualFileManager.getInstance().findFileByUrl(rootUrl) : null;
    }

    @Nonnull
    protected static String getElementText(@Nonnull ParamContext context) {
        return ParamDefImplUtil.getElementText(context.getValueElement());
    }

    @Nullable
    protected static RubyLookupItem createLookupItem(@Nonnull ParamContext context, @Nullable String value, @Nonnull LookupItemType type) {
        return value != null ? ParamDefImplUtil.createSimpleLookupItem(value, type, context.getValueElement()) : null;
    }

    /**
     * @return the lookup items the function builds for the files under the root, null without the root
     */
    @Nullable
    protected static List<RubyLookupItem> collectFiles(@Nonnull ParamContext context, @Nullable VirtualFile root, @Nonnull Function<VirtualFile, RubyLookupItem> builder) {
        if (root == null || context.getProject() == null) {
            return null;
        }
        List<RubyLookupItem> items = new ArrayList<>();
        ProjectRootManager.getInstance(context.getProject()).getFileIndex().iterateContentUnderDirectory(root, file -> {
            if (!file.isDirectory()) {
                RubyLookupItem item = builder.apply(file);
                if (item != null) {
                    items.add(item);
                }
            }
            return true;
        });
        return items;
    }

    @Nullable
    protected static PsiFile findPsiFile(@Nonnull ParamContext context, @Nullable VirtualFile file) {
        return file != null && context.getProject() != null ? PsiManager.getInstance(context.getProject()).findFile(file) : null;
    }

    @Nullable
    protected static PsiFile findPsiFile(@Nonnull ParamContext context, @Nullable VirtualFile root, @Nonnull String relativePath) {
        return root != null ? findPsiFile(context, root.findFileByRelativePath(relativePath)) : null;
    }

    /**
     * @return the file name up to its first dot: {@code index.html.erb} is {@code index}
     */
    @Nonnull
    protected static String getBaseName(@Nonnull VirtualFile file) {
        String name = file.getName();
        int dot = name.indexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
