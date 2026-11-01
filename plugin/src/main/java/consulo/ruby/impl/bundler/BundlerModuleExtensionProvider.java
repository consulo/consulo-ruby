package consulo.ruby.impl.bundler;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.service.module.extension.ExternalSystemModuleExtensionImpl;
import consulo.externalSystem.service.module.extension.ExternalSystemMutableModuleExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.ruby.api.icon.RubyApiIconGroup;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
@ExtensionImpl
public class BundlerModuleExtensionProvider implements ModuleExtensionProvider<ExternalSystemModuleExtensionImpl> {
    @Nonnull
    @Override
    public String getId() {
        return BundlerConstants.SYSTEM_ID.getId();
    }

    @Nullable
    @Override
    public String getParentId() {
        return null;
    }

    @Override
    public boolean isSystemOnly() {
        return true;
    }

    @Nonnull
    @Override
    public LocalizeValue getName() {
        return RubyApiLocalize.bundlerSystemName();
    }

    @Nonnull
    @Override
    public Image getIcon() {
        return RubyApiIconGroup.rubyRubygems();
    }

    @Nonnull
    @Override
    public ModuleExtension<ExternalSystemModuleExtensionImpl> createImmutableExtension(@Nonnull ModuleRootLayer moduleRootLayer) {
        return new ExternalSystemModuleExtensionImpl(getId(), moduleRootLayer, BundlerConstants.SYSTEM_ID);
    }

    @Nonnull
    @Override
    public MutableModuleExtension<ExternalSystemModuleExtensionImpl> createMutableExtension(@Nonnull ModuleRootLayer moduleRootLayer) {
        return new ExternalSystemMutableModuleExtensionImpl(getId(), moduleRootLayer, BundlerConstants.SYSTEM_ID);
    }
}
