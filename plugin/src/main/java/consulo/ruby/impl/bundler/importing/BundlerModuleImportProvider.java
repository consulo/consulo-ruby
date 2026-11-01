package consulo.ruby.impl.bundler.importing;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.importing.AbstractExternalModuleImportProvider;
import consulo.externalSystem.importing.ExternalModuleImportContext;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.service.project.ProjectData;
import consulo.externalSystem.service.project.manage.ProjectDataManager;
import consulo.project.Project;
import consulo.ruby.api.icon.RubyApiIconGroup;
import consulo.ruby.impl.bundler.BundlerConstants;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
@ExtensionImpl
public class BundlerModuleImportProvider extends AbstractExternalModuleImportProvider {
    @Inject
    public BundlerModuleImportProvider(@Nonnull ProjectDataManager dataManager) {
        super(dataManager, BundlerConstants.SYSTEM_ID);
    }

    @Nullable
    @Override
    public Image getIcon() {
        return RubyApiIconGroup.rubyRubygems();
    }

    @Override
    public boolean canImport(@Nonnull Path fileOrDirectory) {
        if (Files.isDirectory(fileOrDirectory)) {
            return Files.isRegularFile(fileOrDirectory.resolve(BundlerConstants.GEMFILE));
        }
        return BundlerConstants.GEMFILE.equals(fileOrDirectory.getFileName().toString()) && Files.isRegularFile(fileOrDirectory);
    }

    @Override
    protected void doPrepare(@Nonnull ExternalModuleImportContext context) {
    }

    @Override
    protected void beforeCommit(@Nonnull DataNode<ProjectData> dataNode, @Nonnull Project project) {
    }

    @Nonnull
    @Override
    protected File getExternalProjectConfigToUse(@Nonnull File file) {
        File directory = file.isDirectory() ? file : file.getParentFile();
        return directory == null ? file : directory;
    }

    @Override
    protected void applyExtraSettings(@Nonnull ExternalModuleImportContext context) {
    }
}
