package consulo.ruby.impl.bundler;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.ExternalSystemManager;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.service.project.ExternalSystemProjectResolver;
import consulo.externalSystem.service.project.autoimport.CachingExternalSystemAutoImportAware;
import consulo.externalSystem.service.project.autoimport.ExternalSystemAutoImportAware;
import consulo.externalSystem.task.ExternalSystemTaskManager;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.project.Project;
import consulo.ruby.impl.bundler.resolve.BundlerProjectResolver;
import consulo.ruby.impl.bundler.setting.BundlerExecutionSettings;
import consulo.ruby.impl.bundler.setting.BundlerLocalSettings;
import consulo.ruby.impl.bundler.setting.BundlerProjectSettings;
import consulo.ruby.impl.bundler.setting.BundlerSettings;
import consulo.ruby.impl.bundler.setting.BundlerSettingsListener;
import consulo.ruby.impl.bundler.task.BundlerTaskManager;
import consulo.util.lang.Pair;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
@ExtensionImpl
public class BundlerManager implements ExternalSystemAutoImportAware,
    ExternalSystemManager<BundlerProjectSettings, BundlerSettingsListener, BundlerSettings, BundlerLocalSettings, BundlerExecutionSettings> {
    private final BundlerAutoImportAware myAutoImportAware = new BundlerAutoImportAware();
    private final ExternalSystemAutoImportAware myAutoImportDelegate = new CachingExternalSystemAutoImportAware(myAutoImportAware);

    @Nonnull
    @Override
    public ProjectSystemId getSystemId() {
        return BundlerConstants.SYSTEM_ID;
    }

    @Nonnull
    @Override
    public Function<Project, BundlerSettings> getSettingsProvider() {
        return BundlerSettings::getInstance;
    }

    @Nonnull
    @Override
    public Function<Project, BundlerLocalSettings> getLocalSettingsProvider() {
        return BundlerLocalSettings::getInstance;
    }

    @Nonnull
    @Override
    public Function<Pair<Project, String>, BundlerExecutionSettings> getExecutionSettingsProvider() {
        return pair -> {
            String projectPath = pair.getSecond();
            BundlerProjectSettings projectSettings = BundlerSettings.getInstance(pair.getFirst()).getLinkedProjectSettings(projectPath);
            return BundlerRubyLocator.createExecutionSettings(projectPath, projectSettings);
        };
    }

    @Nonnull
    @Override
    public Supplier<? extends ExternalSystemProjectResolver<BundlerExecutionSettings>> getProjectResolverFactory() {
        return BundlerProjectResolver::new;
    }

    @Nonnull
    @Override
    public Supplier<? extends ExternalSystemTaskManager<BundlerExecutionSettings>> getTaskManagerFactory() {
        return BundlerTaskManager::new;
    }

    @Nonnull
    @Override
    public FileChooserDescriptor getExternalProjectDescriptor() {
        return new FileChooserDescriptor(true, true, false, false, false, false) {
            @Override
            public boolean isFileSelectable(VirtualFile file) {
                return file.isDirectory() || BundlerConstants.GEMFILE.equals(file.getName());
            }
        };
    }

    @Nullable
    @Override
    public String getAffectedExternalProjectPath(String changedFileOrDirPath, Project project) {
        return myAutoImportDelegate.getAffectedExternalProjectPath(changedFileOrDirPath, project);
    }

    @Override
    public List<Path> getAffectedExternalProjectFilePaths(String projectPath, Project project) {
        return myAutoImportAware.getAffectedExternalProjectFilePaths(projectPath, project);
    }
}
