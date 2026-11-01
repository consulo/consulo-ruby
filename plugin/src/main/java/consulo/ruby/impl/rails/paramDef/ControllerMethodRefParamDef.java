package consulo.ruby.impl.rails.paramDef;

import consulo.language.psi.PsiElement;
import consulo.module.Module;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.rails.nameConventions.ControllersConventions;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.LookupItemType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamContext;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamDependency;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;

/**
 * An action of a controller: the controller of the view around the call, the class around it, or the one {@code :controller} names.
 */
public class ControllerMethodRefParamDef extends MethodRefParamDef {
    public ControllerMethodRefParamDef(@Nullable ParamDependency<PsiElement> classDependency) {
        super(true, LookupItemType.String, classDependency);
    }

    @Nullable
    @Override
    protected RClass findTargetClass(@Nonnull ParamContext context) {
        Module module = context.getModule();
        if (module != null) {
            RClass controller = ControllersConventions.getControllerByViewFile(context.getCall().getContainingFile(), module);
            if (controller != null) {
                return controller;
            }
        }
        return super.findTargetClass(context);
    }
}
