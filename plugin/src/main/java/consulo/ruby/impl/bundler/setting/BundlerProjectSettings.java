package consulo.ruby.impl.bundler.setting;

import consulo.externalSystem.setting.ExternalProjectSettings;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
public class BundlerProjectSettings extends ExternalProjectSettings {
    @Nullable
    private String mySdkName;

    @Nullable
    public String getSdkName() {
        return mySdkName;
    }

    public void setSdkName(@Nullable String sdkName) {
        mySdkName = sdkName;
    }

    @Override
    public BundlerProjectSettings clone() {
        BundlerProjectSettings clone = new BundlerProjectSettings();
        copyTo(clone);
        clone.mySdkName = mySdkName;
        return clone;
    }
}
