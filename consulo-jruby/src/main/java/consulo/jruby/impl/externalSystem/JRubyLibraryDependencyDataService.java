package consulo.jruby.impl.externalSystem;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.LibraryDependencyData;
import consulo.externalSystem.service.project.manage.ProjectDataService;
import consulo.externalSystem.util.DisposeAwareProjectChange;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.extension.MutableModuleExtension;
import consulo.project.Project;
import org.jetbrains.plugins.ruby.jruby.JRubyUtil;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Enables JRuby in the modules that an external build (Gradle, ...) gives a JRuby library: org.jruby:jruby-complete, ...
 */
@ExtensionImpl
public class JRubyLibraryDependencyDataService implements ProjectDataService<LibraryDependencyData, Module> {
    @Override
    public Key<LibraryDependencyData> getTargetDataKey() {
        return ProjectKeys.LIBRARY_DEPENDENCY;
    }

    @Override
    public void importData(Collection<DataNode<LibraryDependencyData>> toImport, Project project, boolean synchronous) {
        Set<String> moduleNames = new LinkedHashSet<>();
        for (DataNode<LibraryDependencyData> node : toImport) {
            LibraryDependencyData dependency = node.getData();
            if (JRubyUtil.isJRubyLibraryName(dependency.getTarget().getExternalName())
                || JRubyUtil.isJRubyLibraryName(dependency.getTarget().getInternalName())) {
                moduleNames.add(dependency.getOwnerModule().getInternalName());
            }
        }
        if (moduleNames.isEmpty()) {
            return;
        }

        ExternalSystemApiUtil.executeProjectChangeAction(synchronous, new DisposeAwareProjectChange(project) {
            @Override
            public void execute() {
                ModuleManager moduleManager = ModuleManager.getInstance(project);
                for (String moduleName : moduleNames) {
                    Module module = moduleManager.findModuleByName(moduleName);
                    if (module != null && !module.isDisposed()) {
                        enableJRuby(module);
                    }
                }
            }
        });
    }

    private static void enableJRuby(Module module) {
        ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();
        MutableModuleExtension<?> extension = rootModel.getExtensionWithoutCheck(JRubyUtil.EXTENSION_ID);
        if (extension == null || extension.isEnabled()) {
            rootModel.dispose();
            return;
        }
        extension.setEnabled(true);
        rootModel.commit();
    }

    @Override
    public void removeData(Collection<? extends Module> toRemove, Project project, boolean synchronous) {
    }
}
