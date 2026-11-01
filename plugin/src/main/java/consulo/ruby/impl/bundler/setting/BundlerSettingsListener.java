package consulo.ruby.impl.bundler.setting;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.TopicAPI;
import consulo.externalSystem.setting.ExternalSystemSettingsListener;

import java.util.Collection;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
@TopicAPI(ComponentScope.PROJECT)
public interface BundlerSettingsListener extends ExternalSystemSettingsListener<BundlerProjectSettings> {
    Class<BundlerSettingsListener> TOPIC = BundlerSettingsListener.class;

    @Override
    default void onProjectRenamed(String oldName, String newName) {
    }

    @Override
    default void onProjectsLoaded(Collection<BundlerProjectSettings> settings) {
    }

    @Override
    default void onProjectsLinked(Collection<BundlerProjectSettings> settings) {
    }

    @Override
    default void onProjectsUnlinked(Set<String> linkedProjectPaths) {
    }

    @Override
    default void onBulkChangeStart() {
    }

    @Override
    default void onBulkChangeEnd() {
    }
}
