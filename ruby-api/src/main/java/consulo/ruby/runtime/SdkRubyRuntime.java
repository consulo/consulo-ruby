package consulo.ruby.runtime;

import consulo.content.bundle.Sdk;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.cache.StubsUrls;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil;

/**
 * A Ruby SDK: its interpreter and the stubs generated for it.
 */
public class SdkRubyRuntime implements RubyRuntime {
    private final Sdk mySdk;

    public SdkRubyRuntime(Sdk sdk) {
        mySdk = sdk;
    }

    public Sdk getSdk() {
        return mySdk;
    }

    @Override
    public String getExecutablePath() {
        return RubySdkUtil.getVMExecutablePath(mySdk);
    }

    @Override
    public String getBuiltInStubsUrl() {
        String stubsDirUrl = RubySdkUtil.getRubyStubsDirUrl(mySdk);
        return stubsDirUrl != null ? stubsDirUrl + StubsUrls.BUILT_IN_RB : BundledRubyStubs.getBuiltInUrl();
    }
}
