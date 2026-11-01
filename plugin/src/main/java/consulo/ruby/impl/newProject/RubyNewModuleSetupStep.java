package consulo.ruby.impl.newProject;

import consulo.disposer.Disposable;
import consulo.module.creation.ui.UnifiedProjectOrModuleNameStep;
import consulo.module.ui.BundleBox;
import consulo.module.ui.BundleBoxBuilder;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkType;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
public class RubyNewModuleSetupStep extends UnifiedProjectOrModuleNameStep<RubyNewModuleWizardContext> {
    @Nullable
    private BundleBox mySdkBox;

    public RubyNewModuleSetupStep(RubyNewModuleWizardContext context) {
        super(context);
    }

    @RequiredUIAccess
    @Override
    protected void extend(FormBuilder builder, Disposable uiDisposable) {
        super.extend(builder, uiDisposable);

        BundleBox sdkBox = BundleBoxBuilder.create(uiDisposable).withSdkTypeFilterByClass(RubySdkType.class).build();
        sdkBox.getComponent().selectFirst();
        mySdkBox = sdkBox;
        builder.addLabeled(RubyApiLocalize.bundlerSettingsSdk(), sdkBox.getComponent());
    }

    @Override
    public void onStepLeave(RubyNewModuleWizardContext context) {
        super.onStepLeave(context);

        BundleBox sdkBox = mySdkBox;
        BundleBox.BundleBoxItem item = sdkBox == null ? null : sdkBox.getComponent().getValue();
        context.setSdk(item == null ? null : item.getBundle());
    }
}
