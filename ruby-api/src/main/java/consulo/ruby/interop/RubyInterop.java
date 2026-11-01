package consulo.ruby.interop;

import consulo.application.Application;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;
import consulo.module.Module;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.ForeignSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.ProxyForeignSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.data.Children;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.Context;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Lookups over the {@link RubyInteropProvider} extensions.
 */
public final class RubyInterop {
    private RubyInterop() {
    }

    /**
     * @return true if some provider serves the module
     */
    public static boolean isEnabled(@Nonnull Module module) {
        return compute(provider -> provider.isEnabled(module) ? Boolean.TRUE : null) != null;
    }

    /**
     * @return the provider of a foreign element, null for Ruby and unknown elements
     */
    @Nullable
    public static RubyInteropProvider forElement(@Nullable PsiElement element) {
        if (element == null) {
            return null;
        }
        return compute(provider -> provider.isForeignElement(element) ? provider : null);
    }

    /**
     * @return the first non-null result of the providers
     */
    @Nullable
    public static <T> T compute(@Nonnull Function<RubyInteropProvider, T> function) {
        return Application.get().getExtensionPoint(RubyInteropProvider.class).computeSafeIfAny(function);
    }

    public static void forEach(@Nonnull Consumer<RubyInteropProvider> consumer) {
        Application.get().getExtensionPoint(RubyInteropProvider.class).forEach(consumer);
    }

    /**
     * @return the foreign element of a foreign or proxy symbol, null for Ruby symbols
     */
    @Nullable
    public static PsiElement getElement(@Nonnull Symbol symbol) {
        if (symbol instanceof ForeignSymbol foreignSymbol) {
            return foreignSymbol.getPsiElement();
        }
        if (symbol instanceof ProxyForeignSymbol proxySymbol) {
            return proxySymbol.getPsiElement();
        }
        return null;
    }

    /**
     * @return the members of a foreign or proxy symbol, null for Ruby symbols
     */
    @Nullable
    public static Children getChildren(@Nullable FileSymbol fileSymbol, @Nonnull Symbol symbol, @Nonnull Context context) {
        RubyInteropProvider provider = forElement(getElement(symbol));
        return provider != null ? provider.getChildren(fileSymbol, symbol, context) : null;
    }

    /**
     * @return the symbol of the package or class a constant path names, e.g. {@code java.util.ArrayList}
     */
    @Nullable
    public static ForeignSymbol findSymbol(@Nonnull Project project, @Nonnull List<String> path) {
        return compute(provider -> {
            PsiElement element = provider.findPackageOrClass(project, path);
            return element != null ? createSymbol(provider, element) : null;
        });
    }

    /**
     * @return a {@code FOREIGN_PACKAGE} or {@code FOREIGN_CLASS} symbol, null for other elements
     */
    @Nullable
    public static ForeignSymbol createSymbol(@Nonnull RubyInteropProvider provider, @Nonnull PsiElement element) {
        String name = element instanceof PsiNamedElement namedElement ? namedElement.getName() : null;
        if (name == null) {
            return null;
        }
        if (provider.isPackage(element)) {
            return new ForeignSymbol(element, name, null, Type.FOREIGN_PACKAGE);
        }
        if (provider.isClass(element)) {
            return new ForeignSymbol(element, name, null, Type.FOREIGN_CLASS);
        }
        return null;
    }
}
