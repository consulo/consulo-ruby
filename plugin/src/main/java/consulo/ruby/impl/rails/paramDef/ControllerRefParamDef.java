package consulo.ruby.impl.rails.paramDef;

import consulo.language.psi.PsiElement;
import consulo.module.Module;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.rails.facet.configuration.StandardRailsPaths;
import org.jetbrains.plugins.ruby.rails.nameConventions.ControllersConventions;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubyLookupItem;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.LookupItemType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamContext;

import java.util.Collection;

/**
 * A controller by its path: {@code 'admin/users'} is {@code Admin::UsersController}.
 */
public class ControllerRefParamDef extends RailsParamDefBase {
    @Nullable
    @Override
    public Collection<RubyLookupItem> getVariants(ParamContext context) {
        Module module = context.getModule();
        if (module == null) {
            return null;
        }
        return collectFiles(context, findRoot(context, StandardRailsPaths::getControllerRootURL), file -> {
            String folderPath = ControllersConventions.getRelativePathOfControllerFolder(file.getParent().getUrl(), module);
            String name = ControllersConventions.getControllerName(file);
            if (folderPath == null || name == null) {
                return null;
            }
            return createLookupItem(context, folderPath.isEmpty() ? name : folderPath + "/" + name, LookupItemType.String);
        });
    }

    @Nullable
    @Override
    public PsiElement resolveReference(ParamContext context) {
        Module module = context.getModule();
        return module != null ? ControllersConventions.getControllerClassByShortName(module, getElementText(context)) : null;
    }
}
