package consulo.ruby.impl.bundler.setting;

import consulo.externalSystem.model.setting.ExternalSystemExecutionSettings;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Objects;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
public class BundlerExecutionSettings extends ExternalSystemExecutionSettings {
    @Nonnull
    private final String myRubyExecutable;
    @Nullable
    private final String mySdkName;

    public BundlerExecutionSettings(@Nonnull String rubyExecutable, @Nullable String sdkName) {
        myRubyExecutable = rubyExecutable;
        mySdkName = sdkName;
    }

    @Nonnull
    public String getRubyExecutable() {
        return myRubyExecutable;
    }

    @Nullable
    public String getSdkName() {
        return mySdkName;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + myRubyExecutable.hashCode();
        result = 31 * result + Objects.hashCode(mySdkName);
        return result;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (!super.equals(o)) {
            return false;
        }
        BundlerExecutionSettings that = (BundlerExecutionSettings) o;
        return myRubyExecutable.equals(that.myRubyExecutable) && Objects.equals(mySdkName, that.mySdkName);
    }
}
