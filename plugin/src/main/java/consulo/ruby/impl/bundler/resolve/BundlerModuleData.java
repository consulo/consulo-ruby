package consulo.ruby.impl.bundler.resolve;

import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.service.project.AbstractExternalEntityData;
import consulo.ruby.impl.bundler.BundlerConstants;
import jakarta.annotation.Nullable;

/**
 * The language side of a Bundler module: which Ruby SDK it uses and whether it is a Rails application.
 *
 * @author VISTALL
 * @since 2026-11-01
 */
public class BundlerModuleData extends AbstractExternalEntityData {
    private static final long serialVersionUID = 1L;

    public static final Key<BundlerModuleData> KEY = Key.create(BundlerModuleData.class, ProjectKeys.MODULE.getProcessingWeight() + 1);

    @Nullable
    private final String mySdkName;
    private final boolean myRails;

    public BundlerModuleData(@Nullable String sdkName, boolean rails) {
        super(BundlerConstants.SYSTEM_ID);
        mySdkName = sdkName;
        myRails = rails;
    }

    @Nullable
    public String getSdkName() {
        return mySdkName;
    }

    public boolean isRails() {
        return myRails;
    }
}
