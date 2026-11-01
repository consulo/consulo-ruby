package consulo.ruby.impl.rails.paramDef;

import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubyLookupItem;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.LookupItemType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamContext;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamDependency;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.AccessModifier;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.holders.utils.RContainerUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A method of the class around the call, or of the class another argument names: {@code before_action :authenticate}.
 */
public class MethodRefParamDef extends RailsParamDefBase {
    private final boolean myPublicOnly;
    private final LookupItemType myItemType;
    @Nullable
    private final ParamDependency<PsiElement> myClassDependency;

    public MethodRefParamDef(boolean publicOnly, @Nonnull LookupItemType itemType, @Nullable ParamDependency<PsiElement> classDependency) {
        myPublicOnly = publicOnly;
        myItemType = itemType;
        myClassDependency = classDependency;
    }

    @Nullable
    @Override
    public Collection<RubyLookupItem> getVariants(ParamContext context) {
        RClass target = getTargetClass(context);
        if (target == null) {
            return null;
        }
        List<RubyLookupItem> items = new ArrayList<>();
        for (RMethod method : PsiTreeUtil.findChildrenOfType(target, RMethod.class)) {
            if (!myPublicOnly || method.getAccessModifier() == AccessModifier.PUBLIC) {
                RubyLookupItem item = createLookupItem(context, method.getName(), myItemType);
                if (item != null) {
                    items.add(item);
                }
            }
        }
        return items;
    }

    @Nullable
    @Override
    public PsiElement resolveReference(ParamContext context) {
        RClass target = getTargetClass(context);
        return target != null ? RContainerUtil.getMethodByName(target, getElementText(context)) : null;
    }

    @Nullable
    protected RClass findTargetClass(@Nonnull ParamContext context) {
        return PsiTreeUtil.getParentOfType(context.getCall(), RClass.class);
    }

    @Nullable
    private RClass getTargetClass(@Nonnull ParamContext context) {
        if (myClassDependency != null && myClassDependency.getValue(context) instanceof RClass dependencyClass) {
            return dependencyClass;
        }
        return findTargetClass(context);
    }
}
