package consulo.ruby.impl.bundler.setting;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.component.persist.StoragePathMacros;
import consulo.externalSystem.setting.AbstractExternalSystemSettings;
import consulo.externalSystem.setting.ExternalSystemSettingsListener;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
@State(name = "BundlerSettings", storages = @Storage(file = StoragePathMacros.PROJECT_CONFIG_DIR + "/bundler"))
public class BundlerSettings extends AbstractExternalSystemSettings<BundlerSettings, BundlerProjectSettings, BundlerSettingsListener>
    implements PersistentStateComponent<BundlerSettingsState> {
    @Inject
    public BundlerSettings(@Nonnull Project project) {
        super(BundlerSettingsListener.TOPIC, project);
    }

    @Nonnull
    public static BundlerSettings getInstance(@Nonnull Project project) {
        return project.getInstance(BundlerSettings.class);
    }

    @Override
    public void subscribe(@Nonnull ExternalSystemSettingsListener<BundlerProjectSettings> listener) {
        getProject().getMessageBus().connect(getProject()).subscribe(BundlerSettingsListener.TOPIC, new BundlerSettingsListenerAdapter(listener));
    }

    @Override
    protected void copyExtraSettingsFrom(@Nonnull BundlerSettings settings) {
    }

    @Override
    protected void checkSettings(@Nonnull BundlerProjectSettings old, @Nonnull BundlerProjectSettings current) {
    }

    @Override
    public BundlerSettingsState getState() {
        BundlerSettingsState state = new BundlerSettingsState();
        fillState(state);
        return state;
    }

    @Override
    public void loadState(@Nonnull BundlerSettingsState state) {
        super.loadState(state);
    }
}
