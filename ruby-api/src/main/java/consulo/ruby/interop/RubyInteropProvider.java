package consulo.ruby.interop;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.language.editor.parameterInfo.ParameterInfoUIContext;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.module.Module;
import consulo.project.Project;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.data.Children;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.Context;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RType;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.basicTypes.stringLiterals.RBaseString;

import java.util.List;

/**
 * Elements of another language used from Ruby code, e.g. Java classes in JRuby: the foreign symbols
 * ({@code FOREIGN_*} types) of a file with interop enabled come from here.
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface RubyInteropProvider {
    /**
     * @return true if the Ruby code of the module may use the foreign elements, e.g. JRuby is among its dependencies
     */
    boolean isEnabled(@Nonnull Module module);

    /**
     * @return true if the provider serves the element (a package, class, method or field of its language)
     */
    boolean isForeignElement(@Nonnull PsiElement element);

    /**
     * @return true for the library a script requires to turn the interop on, e.g. {@code require 'java'}
     */
    boolean isInteropLibrary(@Nonnull String requiredName);

    /**
     * @return the type of a top-level module the interop defines, e.g. {@code Java}, null for other modules
     */
    @Nullable
    RType createModuleType(@Nonnull FileSymbol fileSymbol, @Nonnull Symbol symbol, @Nonnull Context context, boolean inReference);

    /**
     * @return the package or class for a constant path, e.g. {@code java.util.ArrayList}
     */
    @Nullable
    PsiElement findPackageOrClass(@Nonnull Project project, @Nonnull List<String> path);

    @Nullable
    PsiElement findPackageOrClass(@Nonnull Project project, @Nonnull String qualifiedName);

    /**
     * @return the packages and classes completed at the top level
     */
    @Nonnull
    List<PsiElement> getTopLevelPackagesAndClasses(@Nonnull Project project);

    boolean isPackage(@Nonnull PsiElement element);

    boolean isClass(@Nonnull PsiElement element);

    @Nonnull
    List<PsiElement> getPackageClasses(@Nonnull PsiElement foreignPackage);

    @Nonnull
    List<PsiElement> getConstructors(@Nonnull PsiElement foreignClass);

    /**
     * The members of a foreign symbol: methods and fields of a class, classes of a package, the type of a method or field
     */
    @Nonnull
    Children getChildren(@Nullable FileSymbol fileSymbol, @Nonnull Symbol symbol, @Nonnull Context context);

    /**
     * @return the qualified name of a package or class, null for other elements
     */
    @Nullable
    String getQualifiedName(@Nonnull PsiElement element);

    /**
     * @return the qualified name of the class declaring a method or field, null for other elements
     */
    @Nullable
    String getContainingClassName(@Nonnull PsiElement member);

    /**
     * @return the reference text a documentation link to the element is built from
     */
    @Nullable
    String getDocumentationReference(@Nonnull PsiElement element);

    /**
     * @return true for an abstract method a Ruby class implements
     */
    boolean isAbstractMethod(@Nonnull PsiElement method);

    /**
     * @return true for a non-abstract method a Ruby class may override
     */
    boolean isOverridableMethod(@Nonnull PsiElement method);

    /**
     * @return the number of parameters of a method, -1 if the element is not a method
     */
    int getParameterCount(@Nonnull PsiElement method);

    /**
     * @return the parameter names of a method, empty if the element is not a method
     */
    @Nonnull
    List<String> getParameterNames(@Nonnull PsiElement method);

    /**
     * @return true for a static method or field
     */
    boolean isStaticMember(@Nonnull PsiElement member);

    void updateParameterInfo(@Nonnull PsiElement method, @Nonnull ParameterInfoUIContext context);

    /**
     * @return the references of a string naming a package or class, e.g. the argument of {@code java_import}
     */
    @Nonnull
    List<PsiReference> createStringReferences(@Nonnull RPsiElement call, @Nonnull RBaseString string);

    @Nonnull
    Image getIcon();
}
