package consulo.jruby.impl.interop;

import com.intellij.java.impl.codeInsight.hint.api.impls.MethodParameterInfoHandler;
import com.intellij.java.language.impl.codeInsight.javadoc.JavaDocUtil;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.java.language.psi.PsiField;
import com.intellij.java.language.psi.PsiJavaPackage;
import com.intellij.java.language.psi.PsiMember;
import com.intellij.java.language.psi.PsiMethod;
import com.intellij.java.language.psi.PsiModifier;
import com.intellij.java.language.psi.PsiParameter;
import com.intellij.java.language.psi.util.PsiUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.parameterInfo.ParameterInfoUIContext;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.module.Module;
import consulo.project.Project;
import consulo.ruby.api.icon.RubyApiIconGroup;
import consulo.ruby.interop.RubyInterop;
import consulo.ruby.interop.RubyInteropProvider;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.jruby.JRubyUtil;
import org.jetbrains.plugins.ruby.jruby.codeInsight.resolve.JavaReferencesBuilder;
import org.jetbrains.plugins.ruby.jruby.codeInsight.resolve.JavaResolveUtil;
import org.jetbrains.plugins.ruby.jruby.codeInsight.types.JRubyDuckTypeUtil;
import org.jetbrains.plugins.ruby.jruby.codeInsight.types.JavaNamespaceType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.data.Children;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.Context;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RType;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.basicTypes.stringLiterals.RBaseString;

import java.util.ArrayList;
import java.util.List;

/**
 * Java classes, packages and members used from JRuby code.
 */
@ExtensionImpl
public class JRubyInteropProvider implements RubyInteropProvider {
    public static final String JAVA_NAMESPACE = "Java";

    @Override
    public boolean isEnabled(@Nonnull Module module) {
        return JRubyUtil.hasJRubySupport(module);
    }

    @Override
    public boolean isForeignElement(@Nonnull PsiElement element) {
        return element instanceof PsiClass || element instanceof PsiJavaPackage || element instanceof PsiMethod || element instanceof PsiField;
    }

    @Override
    public boolean isInteropLibrary(@Nonnull String requiredName) {
        return JRubyUtil.JAVA.equals(requiredName);
    }

    @Nullable
    @Override
    public RType createModuleType(@Nonnull FileSymbol fileSymbol, @Nonnull Symbol symbol, @Nonnull Context context, boolean inReference) {
        Symbol parent = symbol.getParentSymbol();
        if (parent != null && parent.getType() == Type.FILE && JAVA_NAMESPACE.equals(symbol.getName())) {
            return new JavaNamespaceType(fileSymbol, symbol, context, inReference);
        }
        return null;
    }

    @Nullable
    @Override
    public PsiElement findPackageOrClass(@Nonnull Project project, @Nonnull List<String> path) {
        return JavaResolveUtil.getPackageOrClass(project, path);
    }

    @Nullable
    @Override
    public PsiElement findPackageOrClass(@Nonnull Project project, @Nonnull String qualifiedName) {
        return JavaResolveUtil.getPackageOrClass(project, qualifiedName);
    }

    @Nonnull
    @Override
    public List<PsiElement> getTopLevelPackagesAndClasses(@Nonnull Project project) {
        return JavaResolveUtil.getAllowedTopLevelPackagesAndClasses(project);
    }

    @Override
    public boolean isPackage(@Nonnull PsiElement element) {
        return element instanceof PsiJavaPackage;
    }

    @Override
    public boolean isClass(@Nonnull PsiElement element) {
        return element instanceof PsiClass;
    }

    @Nonnull
    @Override
    public List<PsiElement> getPackageClasses(@Nonnull PsiElement foreignPackage) {
        return foreignPackage instanceof PsiJavaPackage javaPackage ? List.of(javaPackage.getClasses()) : List.of();
    }

    @Nonnull
    @Override
    public List<PsiElement> getConstructors(@Nonnull PsiElement foreignClass) {
        return foreignClass instanceof PsiClass psiClass ? List.of(psiClass.getConstructors()) : List.of();
    }

    @Nonnull
    @Override
    public Children getChildren(@Nullable FileSymbol fileSymbol, @Nonnull Symbol symbol, @Nonnull Context context) {
        PsiElement element = RubyInterop.getElement(symbol);
        if (element instanceof PsiClass psiClass) {
            return JRubyDuckTypeUtil.getChildrenByJavaClass(fileSymbol, psiClass, context);
        }
        if (element instanceof PsiJavaPackage javaPackage) {
            return JRubyDuckTypeUtil.getChildrenByJavaPackage(fileSymbol, javaPackage);
        }
        if (element instanceof PsiMethod method) {
            return JRubyDuckTypeUtil.getChildrenByJavaMethod(fileSymbol, method);
        }
        if (element instanceof PsiField field) {
            return JRubyDuckTypeUtil.getChildrenByJavaField(fileSymbol, field);
        }
        return new Children(null);
    }

    @Nullable
    @Override
    public String getQualifiedName(@Nonnull PsiElement element) {
        if (element instanceof PsiClass psiClass) {
            return psiClass.getQualifiedName();
        }
        if (element instanceof PsiJavaPackage javaPackage) {
            return javaPackage.getQualifiedName();
        }
        return null;
    }

    @Nullable
    @Override
    public String getContainingClassName(@Nonnull PsiElement member) {
        if ((member instanceof PsiMethod || member instanceof PsiField) && member instanceof PsiMember psiMember) {
            PsiClass containingClass = psiMember.getContainingClass();
            return containingClass != null ? containingClass.getQualifiedName() : null;
        }
        return null;
    }

    @Nullable
    @Override
    public String getDocumentationReference(@Nonnull PsiElement element) {
        return JavaDocUtil.getReferenceText(element.getProject(), element);
    }

    @Override
    public boolean isAbstractMethod(@Nonnull PsiElement method) {
        return method instanceof PsiMethod psiMethod && isAbstract(psiMethod);
    }

    @Override
    public boolean isOverridableMethod(@Nonnull PsiElement method) {
        return method instanceof PsiMethod psiMethod && PsiUtil.canBeOverriden(psiMethod) && !isAbstract(psiMethod);
    }

    @Override
    public int getParameterCount(@Nonnull PsiElement method) {
        return method instanceof PsiMethod psiMethod ? psiMethod.getParameterList().getParametersCount() : -1;
    }

    @Nonnull
    @Override
    public List<String> getParameterNames(@Nonnull PsiElement method) {
        if (!(method instanceof PsiMethod psiMethod)) {
            return List.of();
        }
        List<String> names = new ArrayList<>();
        for (PsiParameter parameter : psiMethod.getParameterList().getParameters()) {
            names.add(parameter.getName());
        }
        return names;
    }

    @Override
    public boolean isStaticMember(@Nonnull PsiElement member) {
        return member instanceof PsiMember psiMember && psiMember.hasModifierProperty(PsiModifier.STATIC);
    }

    @Override
    public void updateParameterInfo(@Nonnull PsiElement method, @Nonnull ParameterInfoUIContext context) {
        if (method instanceof PsiMethod psiMethod) {
            MethodParameterInfoHandler.updateMethodPresentation(psiMethod, null, context);
        }
    }

    @Nonnull
    @Override
    public List<PsiReference> createStringReferences(@Nonnull RPsiElement call, @Nonnull RBaseString string) {
        return JavaReferencesBuilder.createReferences(call, string);
    }

    @Nonnull
    @Override
    public Image getIcon() {
        return RubyApiIconGroup.jrubyJruby();
    }

    private static boolean isAbstract(PsiMethod method) {
        PsiClass containingClass = method.getContainingClass();
        return method.hasModifierProperty(PsiModifier.ABSTRACT) || containingClass != null && containingClass.isInterface();
    }
}
