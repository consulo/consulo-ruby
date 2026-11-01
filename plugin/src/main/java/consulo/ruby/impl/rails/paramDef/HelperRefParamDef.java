package consulo.ruby.impl.rails.paramDef;

import consulo.language.psi.PsiElement;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.rails.facet.configuration.StandardRailsPaths;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubyLookupItem;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.LookupItemType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamContext;

import java.util.Collection;

/**
 * A helper module by its path: {@code helper :admin} is {@code app/helpers/admin_helper.rb}.
 */
public class HelperRefParamDef extends RailsParamDefBase {
    private static final String SUFFIX = "_helper.rb";

    @Nullable
    @Override
    public Collection<RubyLookupItem> getVariants(ParamContext context) {
        VirtualFile root = findRoot(context, StandardRailsPaths::getHelpersRootURL);
        return collectFiles(context, root, file -> {
            String relative = VirtualFileUtil.getRelativePath(file, root, '/');
            if (relative == null || !relative.endsWith(SUFFIX)) {
                return null;
            }
            String name = relative.substring(0, relative.length() - SUFFIX.length());
            return createLookupItem(context, name.contains("/") ? "'" + name + "'" : name, LookupItemType.Symbol);
        });
    }

    @Nullable
    @Override
    public PsiElement resolveReference(ParamContext context) {
        return findPsiFile(context, findRoot(context, StandardRailsPaths::getHelpersRootURL), getElementText(context) + SUFFIX);
    }
}
