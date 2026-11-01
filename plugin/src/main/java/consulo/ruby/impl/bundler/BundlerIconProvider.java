package consulo.ruby.impl.bundler;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.ui.ExternalSystemIconProvider;
import consulo.ruby.api.icon.RubyApiIconGroup;
import consulo.ui.image.Image;

/**
 * @author VISTALL
 * @since 2026-11-01
 */
@ExtensionImpl
public class BundlerIconProvider implements ExternalSystemIconProvider {
    @Override
    public ProjectSystemId getSystemId() {
        return BundlerConstants.SYSTEM_ID;
    }

    @Override
    public Image getProjectIcon() {
        return RubyApiIconGroup.rubyRubygems();
    }
}
