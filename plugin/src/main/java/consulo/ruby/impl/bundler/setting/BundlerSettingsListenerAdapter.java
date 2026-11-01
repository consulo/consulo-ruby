package consulo.ruby.impl.bundler.setting;

import consulo.externalSystem.setting.DelegatingExternalSystemSettingsListener;
import consulo.externalSystem.setting.ExternalSystemSettingsListener;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
class BundlerSettingsListenerAdapter extends DelegatingExternalSystemSettingsListener<BundlerProjectSettings> implements BundlerSettingsListener {
    BundlerSettingsListenerAdapter(ExternalSystemSettingsListener<BundlerProjectSettings> delegate) {
        super(delegate);
    }
}
