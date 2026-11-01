package consulo.ruby.impl.bundler.setting;

import consulo.configurable.ConfigurationException;
import consulo.disposer.Disposable;
import consulo.externalSystem.service.setting.AbstractExternalProjectSettingsConfigurable;
import consulo.externalSystem.service.setting.ExternalSystemSettingsPlace;
import consulo.module.ui.BundleBox;
import consulo.module.ui.BundleBoxBuilder;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkType;

import java.util.Objects;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
public class BundlerProjectSettingsConfigurable extends AbstractExternalProjectSettingsConfigurable<BundlerProjectSettings> {
    @Nullable
    private BundleBox mySdkBox;

    public BundlerProjectSettingsConfigurable(BundlerProjectSettings settings, ExternalSystemSettingsPlace place) {
        super(settings, place);
    }

    @Override
    @RequiredUIAccess
    protected Component createExtraUIComponent(Disposable uiDisposable) {
        BundleBox sdkBox = BundleBoxBuilder.create(uiDisposable)
            .withSdkTypeFilterByClass(RubySdkType.class)
            .withNoneItem(RubyApiLocalize.bundlerSettingsSdkAuto())
            .build();

        String sdkName = getSettings().getSdkName();
        if (sdkName == null) {
            sdkBox.setSelectedNoneBundle();
        }
        else {
            sdkBox.setSelectedBundle(sdkName);
        }
        mySdkBox = sdkBox;

        return FormBuilder.create()
            .addLabeled(RubyApiLocalize.bundlerSettingsSdk(), sdkBox.getComponent())
            .build();
    }

    @Override
    @RequiredUIAccess
    protected boolean isExtraModified() {
        return mySdkBox != null && !Objects.equals(mySdkBox.getSelectedBundleName(), getSettings().getSdkName());
    }

    @Override
    @RequiredUIAccess
    protected void applyExtra() throws ConfigurationException {
        if (mySdkBox != null) {
            getSettings().setSdkName(mySdkBox.getSelectedBundleName());
        }
    }
}
