package consulo.ruby.impl.bundler.task;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.service.execution.AbstractExternalSystemTaskConfigurationType;
import consulo.ruby.impl.bundler.BundlerConstants;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
@ExtensionImpl
public class BundlerExternalTaskConfigurationType extends AbstractExternalSystemTaskConfigurationType {
    public BundlerExternalTaskConfigurationType() {
        super(BundlerConstants.SYSTEM_ID);
    }
}
