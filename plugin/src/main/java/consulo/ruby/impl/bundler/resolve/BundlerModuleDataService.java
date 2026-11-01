package consulo.ruby.impl.bundler.resolve;

import consulo.annotation.component.ExtensionImpl;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.service.project.manage.ProjectDataService;
import consulo.externalSystem.util.DisposeAwareProjectChange;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.module.Module;
import consulo.module.ModuleManager;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.module.content.layer.orderEntry.ModuleExtensionWithSdkOrderEntry;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.module.extension.MutableModuleExtension;
import consulo.module.extension.MutableModuleExtensionWithSdk;
import consulo.project.Project;
import consulo.ruby.impl.bundler.BundlerConstants;
import org.jetbrains.plugins.ruby.rails.RailsUtil;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Turns a Bundler module into a Ruby module: the {@code ruby} extension with the SDK, and the
 * {@code ruby-on-rails} extension for Rails applications when the Rails plugin is installed.
 *
 * @author VISTALL
 * @since 2026-11-01
 */
@ExtensionImpl
public class BundlerModuleDataService implements ProjectDataService<BundlerModuleData, Module> {
    @Override
    public Key<BundlerModuleData> getTargetDataKey() {
        return BundlerModuleData.KEY;
    }

    @Override
    public void importData(Collection<DataNode<BundlerModuleData>> toImport, Project project, boolean synchronous) {
        Map<String, BundlerModuleData> dataByModule = new LinkedHashMap<>();
        for (DataNode<BundlerModuleData> node : toImport) {
            ModuleData moduleData = node.getData(ProjectKeys.MODULE);
            if (moduleData != null) {
                dataByModule.put(moduleData.getInternalName(), node.getData());
            }
        }
        if (dataByModule.isEmpty()) {
            return;
        }

        ExternalSystemApiUtil.executeProjectChangeAction(synchronous, new DisposeAwareProjectChange(project) {
            @Override
            public void execute() {
                ModuleManager moduleManager = ModuleManager.getInstance(project);
                for (Map.Entry<String, BundlerModuleData> entry : dataByModule.entrySet()) {
                    Module module = moduleManager.findModuleByName(entry.getKey());
                    if (module != null && !module.isDisposed()) {
                        apply(module, entry.getValue());
                    }
                }
            }
        });
    }

    private static void apply(Module module, BundlerModuleData data) {
        ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();

        MutableModuleExtensionWithSdk<?> rubyExtension = rootModel.getExtensionWithoutCheck(BundlerConstants.RUBY_EXTENSION_ID);
        if (rubyExtension != null) {
            rubyExtension.setEnabled(true);

            String sdkName = data.getSdkName();
            Sdk sdk = sdkName == null ? null : SdkTable.getInstance().findSdk(sdkName);
            if (sdk != null) {
                rubyExtension.getInheritableSdk().set(null, sdk);
                if (!hasSdkEntry(rootModel, rubyExtension.getId())) {
                    rootModel.addModuleExtensionSdkEntry(rubyExtension);
                }
            }
        }

        MutableModuleExtension<?> railsExtension = rootModel.getExtensionWithoutCheck(BundlerConstants.RAILS_EXTENSION_ID);
        if (railsExtension != null) {
            railsExtension.setEnabled(data.isRails());
        }

        rootModel.commit();

        if (data.isRails() && railsExtension != null) {
            RailsUtil.ensureRailsServerConfiguration(module);
        }
    }

    private static boolean hasSdkEntry(ModifiableRootModel rootModel, String extensionId) {
        for (OrderEntry orderEntry : rootModel.getOrderEntries()) {
            if (orderEntry instanceof ModuleExtensionWithSdkOrderEntry sdkEntry && extensionId.equals(sdkEntry.getModuleExtensionId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeData(Collection<? extends Module> toRemove, Project project, boolean synchronous) {
    }
}
