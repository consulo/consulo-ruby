package consulo.ruby.impl.rails.paramDef;

import consulo.language.psi.PsiElement;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.rails.facet.configuration.StandardRailsPaths;
import org.jetbrains.plugins.ruby.ruby.codeInsight.completion.RubyLookupItem;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.LookupItemType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An asset by its path under the asset folders, the extension optional when the folders hold one kind of files:
 * {@code stylesheet_link_tag 'application'}, {@code javascript_include_tag 'app'}, {@code image_tag 'logo.png'}.
 * The folders are searched in order (the Rails 7+ ones before the {@code public} ones of old applications); a leading
 * slash starts at the public folder.
 */
public class AssetRefParamDef extends RailsParamDefBase {
    private final List<String> myRoots;
    @Nullable
    private final String myExtension;

    /**
     * @param extension the extension the name may omit, null if names keep their extension
     * @param roots     the asset folders relative to the application root
     */
    public AssetRefParamDef(@Nullable String extension, @Nonnull String... roots) {
        myExtension = extension;
        myRoots = List.of(roots);
    }

    @Nullable
    @Override
    public Collection<RubyLookupItem> getVariants(ParamContext context) {
        Map<String, RubyLookupItem> items = new LinkedHashMap<>();
        for (VirtualFile root : findRoots(context)) {
            List<RubyLookupItem> rootItems = collectFiles(context, root, file -> {
                String name = getAssetName(root, file);
                return name != null ? createLookupItem(context, name, LookupItemType.String) : null;
            });
            if (rootItems != null) {
                for (RubyLookupItem item : rootItems) {
                    items.putIfAbsent(item.getName(), item);
                }
            }
        }
        return items.isEmpty() ? null : items.values();
    }

    @Nullable
    @Override
    public PsiElement resolveReference(ParamContext context) {
        String name = getElementText(context);
        if (myExtension != null && !name.endsWith("." + myExtension)) {
            name = name + "." + myExtension;
        }
        if (name.startsWith("/")) {
            return findPsiFile(context, findRoot(context, StandardRailsPaths::getPublicRootURL), name.substring(1));
        }
        for (VirtualFile root : findRoots(context)) {
            PsiElement file = findPsiFile(context, root, name);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    @Nullable
    private String getAssetName(@Nonnull VirtualFile root, @Nonnull VirtualFile file) {
        String relative = VirtualFileUtil.getRelativePath(file, root, '/');
        if (relative == null) {
            return null;
        }
        if (myExtension == null) {
            return relative;
        }
        if (!myExtension.equalsIgnoreCase(file.getExtension())) {
            return null;
        }
        return relative.substring(0, relative.length() - myExtension.length() - 1);
    }

    @Nonnull
    private List<VirtualFile> findRoots(@Nonnull ParamContext context) {
        VirtualFile application = findRoot(context, StandardRailsPaths::getRailsApplicationRootURL);
        List<VirtualFile> roots = new ArrayList<>();
        if (application != null) {
            for (String path : myRoots) {
                VirtualFile root = application.findFileByRelativePath(path);
                if (root != null && root.isDirectory()) {
                    roots.add(root);
                }
            }
        }
        return roots;
    }
}
