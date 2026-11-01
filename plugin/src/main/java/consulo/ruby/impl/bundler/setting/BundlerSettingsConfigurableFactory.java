package consulo.ruby.impl.bundler.setting;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.service.setting.AbstractExternalProjectSettingsConfigurable;
import consulo.externalSystem.service.setting.ExternalSystemSettingsConfigurableFactory;
import consulo.externalSystem.service.setting.ExternalSystemSettingsPlace;
import consulo.project.ProjectManager;
import consulo.ruby.impl.bundler.BundlerConstants;
import consulo.ui.annotation.RequiredUIAccess;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
@ExtensionImpl
public class BundlerSettingsConfigurableFactory implements ExternalSystemSettingsConfigurableFactory<BundlerProjectSettings, BundlerSettings> {
    @Override
    public ProjectSystemId getSystemId() {
        return BundlerConstants.SYSTEM_ID;
    }

    @Override
    public BundlerProjectSettings createProjectSettings() {
        return new BundlerProjectSettings();
    }

    @Override
    public BundlerSettings createSystemSettings() {
        return new BundlerSettings(ProjectManager.getInstance().getDefaultProject());
    }

    @Override
    @RequiredUIAccess
    public AbstractExternalProjectSettingsConfigurable<BundlerProjectSettings> createProjectSettingsConfigurable(BundlerProjectSettings settings,
                                                                                                                ExternalSystemSettingsPlace place) {
        return new BundlerProjectSettingsConfigurable(settings, place);
    }
}
