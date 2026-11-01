package consulo.ruby.impl.bundler.setting;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.component.persist.StoragePathMacros;
import consulo.externalSystem.setting.AbstractExternalSystemLocalSettings;
import consulo.project.Project;
import consulo.ruby.impl.bundler.BundlerConstants;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
@State(name = "BundlerLocalSettings", storages = @Storage(file = StoragePathMacros.WORKSPACE_FILE))
public class BundlerLocalSettings extends AbstractExternalSystemLocalSettings
    implements PersistentStateComponent<AbstractExternalSystemLocalSettings.State> {
    @Inject
    public BundlerLocalSettings(@Nonnull Project project) {
        super(BundlerConstants.SYSTEM_ID, project);
    }

    @Nonnull
    public static BundlerLocalSettings getInstance(@Nonnull Project project) {
        return project.getInstance(BundlerLocalSettings.class);
    }

    @Nullable
    @Override
    public State getState() {
        State state = new State();
        fillState(state);
        return state;
    }

    @Override
    public void loadState(@Nonnull State state) {
        super.loadState(state);
    }
}
