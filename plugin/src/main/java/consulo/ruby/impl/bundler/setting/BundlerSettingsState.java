package consulo.ruby.impl.bundler.setting;

import consulo.externalSystem.setting.AbstractExternalSystemSettings;
import consulo.util.xml.serializer.annotation.AbstractCollection;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
public class BundlerSettingsState implements AbstractExternalSystemSettings.State<BundlerProjectSettings> {
    private Set<BundlerProjectSettings> myProjectSettings = new TreeSet<>();

    @Override
    @AbstractCollection(surroundWithTag = false, elementTypes = {BundlerProjectSettings.class})
    public Set<BundlerProjectSettings> getLinkedExternalProjectsSettings() {
        return myProjectSettings;
    }

    @Override
    public void setLinkedExternalProjectsSettings(Set<BundlerProjectSettings> settings) {
        if (settings != null) {
            myProjectSettings.addAll(settings);
        }
    }
}
