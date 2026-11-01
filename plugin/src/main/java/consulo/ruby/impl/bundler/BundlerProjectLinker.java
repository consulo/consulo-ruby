package consulo.ruby.impl.bundler;

import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.ExternalSystemDataKeys;
import consulo.externalSystem.model.task.ProgressExecutionMode;
import consulo.externalSystem.service.project.ExternalProjectRefreshCallback;
import consulo.externalSystem.service.project.ExternalSystemProjectRefresher;
import consulo.externalSystem.service.project.ProjectData;
import consulo.externalSystem.service.project.manage.ProjectDataManager;
import consulo.externalSystem.util.DisposeAwareProjectChange;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.module.content.ProjectRootManager;
import consulo.project.Project;
import consulo.ruby.impl.bundler.setting.BundlerLocalSettings;
import consulo.ruby.impl.bundler.setting.BundlerProjectSettings;
import consulo.ruby.impl.bundler.setting.BundlerSettings;
import consulo.ui.annotation.RequiredUIAccess;
import jakarta.annotation.Nullable;

import java.nio.file.Path;
import java.util.Collections;

/**
 * Links a Gemfile directory to a project as a Bundler project and imports it.
 *
 * @author VISTALL
 * @since 2026-11-01
 */
public final class BundlerProjectLinker {
    private BundlerProjectLinker() {
    }

    @RequiredUIAccess
    public static void link(Project project, Path rootDir, @Nullable String sdkName, @Nullable Runnable onImported) {
        if (project.isDisposed() || project.isDefault()) {
            return;
        }

        // the import reads them inside a write action, where a first service creation is not allowed
        BundlerLocalSettings.getInstance(project);

        String path = ExternalSystemApiUtil.toCanonicalPath(rootDir.toString());
        BundlerSettings settings = BundlerSettings.getInstance(project);
        if (settings.getLinkedProjectSettings(path) == null) {
            BundlerProjectSettings projectSettings = new BundlerProjectSettings();
            projectSettings.setExternalProjectPath(path);
            projectSettings.setSdkName(sdkName);
            settings.linkProject(projectSettings);
        }
        project.putUserData(ExternalSystemDataKeys.NEWLY_IMPORTED_PROJECT, Boolean.TRUE);

        ExternalSystemProjectRefresher.getInstance().refreshProject(project,
            BundlerConstants.SYSTEM_ID,
            path,
            new ExternalProjectRefreshCallback() {
                @Override
                public void onSuccess(@Nullable DataNode<ProjectData> externalProject) {
                    if (externalProject != null) {
                        importData(project, externalProject);
                    }
                    if (onImported != null) {
                        project.getApplication().invokeLater(onImported, project.getDisposed());
                    }
                }

                @Override
                public void onFailure(String errorMessage, @Nullable String errorDetails) {
                }
            },
            false,
            ProgressExecutionMode.IN_BACKGROUND_ASYNC);
    }

    private static void importData(Project project, DataNode<ProjectData> externalProject) {
        ExternalSystemApiUtil.executeProjectChangeAction(true, new DisposeAwareProjectChange(project) {
            @RequiredUIAccess
            @Override
            public void execute() {
                ProjectDataManager projectDataManager = project.getApplication().getInstance(ProjectDataManager.class);
                ProjectRootManager.getInstance(project).mergeRootsChangesDuring(
                    () -> projectDataManager.importData(externalProject.getKey(), Collections.singleton(externalProject), project, true)
                );
            }
        });
    }
}
