package consulo.jruby.maven;

import consulo.annotation.component.ExtensionImpl;
import consulo.module.Module;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.extension.MutableModuleExtension;
import org.jetbrains.idea.maven.importing.MavenImporter;
import org.jetbrains.idea.maven.importing.MavenModifiableModelsProvider;
import org.jetbrains.idea.maven.importing.MavenRootModelAdapter;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectChanges;
import org.jetbrains.idea.maven.project.MavenProjectsProcessorTask;
import org.jetbrains.idea.maven.project.MavenProjectsTree;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Enables JRuby in the Maven modules that depend on JRuby: org.jruby:jruby-complete, org.jruby:jruby, ...
 */
@ExtensionImpl
public class JRubyMavenImporter extends MavenImporter {
    private static final String EXTENSION_ID = "jruby";
    private static final String GROUP_ID = "org.jruby";
    private static final Set<String> ARTIFACT_IDS = Set.of("jruby", "jruby-complete", "jruby-core", "jruby-base");

    @Override
    protected String getId() {
        return "dependency:" + GROUP_ID;
    }

    @Override
    public boolean isApplicable(MavenProject mavenProject) {
        for (String artifactId : ARTIFACT_IDS) {
            if (!mavenProject.findDependencies(GROUP_ID, artifactId).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void preProcess(Module module,
                           MavenProject mavenProject,
                           MavenProjectChanges changes,
                           MavenModifiableModelsProvider modifiableModelsProvider) {
    }

    @Override
    public void process(MavenModifiableModelsProvider modifiableModelsProvider,
                        Module module,
                        MavenRootModelAdapter rootModel,
                        MavenProjectsTree mavenModel,
                        MavenProject mavenProject,
                        MavenProjectChanges changes,
                        Map<MavenProject, String> mavenProjectToModuleName,
                        List<MavenProjectsProcessorTask> postTasks) {
        ModifiableRootModel model = modifiableModelsProvider.getRootModel(module);
        MutableModuleExtension<?> extension = model.getExtensionWithoutCheck(EXTENSION_ID);
        if (extension != null) {
            extension.setEnabled(true);
        }
    }
}
