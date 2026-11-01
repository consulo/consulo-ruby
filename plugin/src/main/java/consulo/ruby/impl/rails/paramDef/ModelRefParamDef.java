package consulo.ruby.impl.rails.paramDef;

import consulo.language.psi.PsiElement;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.rails.facet.configuration.StandardRailsPaths;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubyLookupItem;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.LookupItemType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamContext;

import java.util.Collection;

/**
 * A model by its association name: {@code has_many :users} and {@code belongs_to :user} are {@code app/models/user.rb}.
 */
public class ModelRefParamDef extends RailsParamDefBase {
    private final boolean myPlural;

    public ModelRefParamDef(boolean plural) {
        myPlural = plural;
    }

    @Nullable
    @Override
    public Collection<RubyLookupItem> getVariants(ParamContext context) {
        return collectFiles(context, findRoot(context, StandardRailsPaths::getModelRootURL),
            file -> createLookupItem(context, myPlural ? StringUtil.pluralize(file.getNameWithoutExtension()) : file.getNameWithoutExtension(), LookupItemType.Symbol));
    }

    @Nullable
    @Override
    public PsiElement resolveReference(ParamContext context) {
        VirtualFile root = findRoot(context, StandardRailsPaths::getModelRootURL);
        String singular = myPlural ? StringUtil.unpluralize(getElementText(context)) : getElementText(context);
        return singular != null ? findPsiFile(context, root, singular + ".rb") : null;
    }
}
