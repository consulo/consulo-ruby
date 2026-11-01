package consulo.ruby.impl.rails;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiDirectory;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.ui.view.tree.ProjectViewNode;
import consulo.project.ui.view.tree.ProjectViewNodeDecorator;
import consulo.ruby.api.icon.RubyApiIconGroup;
import consulo.ui.ex.tree.PresentationData;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.plugins.ruby.rails.facet.RailsFacetUtil;
import org.jetbrains.plugins.ruby.rails.facet.RailsModuleSupport;

import java.util.Map;

/**
 * Rails icons for the conventional folders of Rails applications in the project view.
 */
@ExtensionImpl
public class RailsProjectViewNodeDecorator implements ProjectViewNodeDecorator {
    private static final Map<String, Image> FOLDER_ICONS = Map.of(
        "app/controllers", RubyApiIconGroup.railsProjectviewControllersfolder(),
        "app/helpers", RubyApiIconGroup.railsProjectviewHelpersfolder(),
        "app/models", RubyApiIconGroup.rubyModels(),
        "db/migrate", RubyApiIconGroup.railsProjectviewMigrationsfolder()
    );

    @RequiredReadAction
    @Override
    public void decorate(ProjectViewNode node, PresentationData data) {
        if (!(node.getValue() instanceof PsiDirectory directory)) {
            return;
        }
        VirtualFile file = directory.getVirtualFile();
        Module module = ModuleUtilCore.findModuleForFile(file, directory.getProject());
        if (!RailsModuleSupport.isEnabled(module)) {
            return;
        }
        String root = RailsFacetUtil.getRailsAppHomeDirPath(module);
        if (root == null || !file.getPath().startsWith(root)) {
            return;
        }
        String relativePath = file.getPath().equals(root) ? "" : file.getPath().substring(root.length() + 1);
        Image icon = relativePath.isEmpty() ? RubyApiIconGroup.railsProjectviewRailsmodulefolder() : FOLDER_ICONS.get(relativePath);
        if (icon != null) {
            data.setIcon(icon);
        }
    }
}
