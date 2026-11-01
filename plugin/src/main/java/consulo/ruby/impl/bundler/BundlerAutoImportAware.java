package consulo.ruby.impl.bundler;

import consulo.externalSystem.service.project.autoimport.ExternalSystemAutoImportAware;
import consulo.project.Project;
import consulo.ruby.impl.bundler.setting.BundlerProjectSettings;
import consulo.ruby.impl.bundler.setting.BundlerSettings;
import consulo.util.io.PathUtil;
import jakarta.annotation.Nullable;

import java.nio.file.Path;
import java.util.List;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
public class BundlerAutoImportAware implements ExternalSystemAutoImportAware {
    @Nullable
    @Override
    public String getAffectedExternalProjectPath(String changedFileOrDirPath, Project project) {
        if (!BundlerPaths.isConfigFileName(PathUtil.getFileName(changedFileOrDirPath))) {
            return null;
        }

        Path parent = Path.of(changedFileOrDirPath).toAbsolutePath().normalize().getParent();
        if (parent == null) {
            return null;
        }

        for (BundlerProjectSettings settings : BundlerSettings.getInstance(project).getLinkedProjectsSettings()) {
            String externalProjectPath = settings.getExternalProjectPath();
            if (externalProjectPath != null && BundlerPaths.getRootDir(externalProjectPath).equals(parent)) {
                return externalProjectPath;
            }
        }
        return null;
    }

    @Override
    public List<Path> getAffectedExternalProjectFilePaths(String projectPath, Project project) {
        return BundlerPaths.getConfigFiles(BundlerPaths.getRootDir(projectPath));
    }
}
